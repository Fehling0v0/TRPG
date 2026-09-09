package com.coc.card.service;

import com.coc.card.dto.auth.LoginRequest;
import com.coc.card.dto.auth.LoginResponse;
import com.coc.card.dto.auth.RegisterRequest;
import com.coc.card.dto.auth.UserInfo;
import com.coc.card.entity.InviteCode;
import com.coc.card.entity.User;
import com.coc.card.exception.BusinessException;
import com.coc.card.repository.InviteCodeRepository;
import com.coc.card.repository.UserRepository;
import com.coc.card.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务：注册（邀请码校验）、登录（BCrypt 校验 + 签发 JWT）
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       InviteCodeRepository inviteCodeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.inviteCodeRepository = inviteCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 注册：校验邀请码有效且未使用 → 创建用户 → 标记邀请码已用
     */
    @Transactional
    public UserInfo register(RegisterRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw BusinessException.badRequest("用户名已存在");
        }

        InviteCode inviteCode = inviteCodeRepository.findByCode(request.inviteCode().trim())
                .orElseThrow(() -> BusinessException.badRequest("邀请码无效"));
        if (Boolean.TRUE.equals(inviteCode.getUsed())) {
            throw BusinessException.badRequest("邀请码已被使用");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(User.Role.USER);
        userRepository.save(user);

        inviteCode.setUsed(true);
        inviteCode.setUsedBy(user.getId());
        inviteCodeRepository.save(inviteCode);

        return toUserInfo(user);
    }

    /**
     * 登录：校验用户名密码，签发 JWT
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> BusinessException.badRequest("用户名或密码错误"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole().name());
        return new LoginResponse(token, "Bearer", toUserInfo(user));
    }

    @Transactional(readOnly = true)
    public UserInfo currentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在或登录状态已失效"));
        return toUserInfo(user);
    }

    /**
     * 修改密码：校验原密码 → 更新为新密码（BCrypt 加密）
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在或登录状态已失效"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BusinessException.badRequest("原密码错误");
        }
        if (oldPassword.equals(newPassword)) {
            throw BusinessException.badRequest("新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public static UserInfo toUserInfo(User user) {
        return new UserInfo(user.getId(), user.getUsername(), user.getRole().name(),
                user.getDisplayFields() == null ? java.util.Collections.emptyList() : user.getDisplayFields());
    }
}
