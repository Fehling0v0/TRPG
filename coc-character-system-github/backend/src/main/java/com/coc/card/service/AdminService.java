package com.coc.card.service;

import com.coc.card.dto.admin.AdminUserView;
import com.coc.card.dto.admin.InviteCodeView;
import com.coc.card.entity.InviteCode;
import com.coc.card.entity.User;
import com.coc.card.exception.BusinessException;
import com.coc.card.repository.InviteCodeRepository;
import com.coc.card.repository.PcRepository;
import com.coc.card.repository.UserRepository;
import com.coc.card.security.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/**
 * 管理员服务：邀请码管理、用户管理
 */
@Service
public class AdminService {

    /** 邀请码字符集（去除易混淆字符 0/O、1/I/L） */
    private static final String CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final String PASSWORD_ALPHABET = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final int PASSWORD_LENGTH = 12;

    private final SecureRandom random = new SecureRandom();
    private final InviteCodeRepository inviteCodeRepository;
    private final UserRepository userRepository;
    private final PcRepository pcRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(InviteCodeRepository inviteCodeRepository,
                        UserRepository userRepository,
                        PcRepository pcRepository,
                        PasswordEncoder passwordEncoder) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.userRepository = userRepository;
        this.pcRepository = pcRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------- 邀请码 ----------------

    @Transactional(readOnly = true)
    public List<InviteCodeView> listInviteCodes() {
        return inviteCodeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(c -> new InviteCodeView(c.getId(), c.getCode(), c.getUsed(),
                        c.getCreatedBy(), c.getUsedBy(), c.getCreatedAt()))
                .toList();
    }

    @Transactional
    public InviteCodeView createInviteCode(Long adminUserId) {
        String code;
        do {
            code = randomString(CODE_ALPHABET, CODE_LENGTH);
        } while (inviteCodeRepository.existsByCode(code));

        InviteCode entity = new InviteCode();
        entity.setCode(code);
        entity.setCreatedBy(adminUserId);
        entity.setUsed(false);
        entity = inviteCodeRepository.save(entity);
        return new InviteCodeView(entity.getId(), entity.getCode(), entity.getUsed(),
                entity.getCreatedBy(), entity.getUsedBy(), entity.getCreatedAt());
    }

    @Transactional
    public void deleteInviteCode(Long id) {
        InviteCode code = inviteCodeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("邀请码不存在"));
        if (Boolean.TRUE.equals(code.getUsed())) {
            throw BusinessException.badRequest("已使用的邀请码不能删除");
        }
        inviteCodeRepository.delete(code);
    }

    // ---------------- 用户 ----------------

    @Transactional(readOnly = true)
    public List<AdminUserView> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new AdminUserView(u.getId(), u.getUsername(), u.getRole().name(),
                        pcRepository.countByUserId(u.getId())))
                .toList();
    }

    /**
     * 删除用户：角色卡由数据库外键级联删除；
     * 该用户生成的邀请码先清理（created_by 外键为 RESTRICT）。
     */
    @Transactional
    public void deleteUser(Long userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        if (target.getId().equals(SecurityUtil.currentUserId())) {
            throw BusinessException.badRequest("不能删除当前登录的管理员账号");
        }
        inviteCodeRepository.deleteAll(inviteCodeRepository.findByCreatedBy(userId));
        userRepository.delete(target);
    }

    /**
     * 重置密码为随机字符串，返回明文新密码（仅返回这一次）
     */
    @Transactional
    public String resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        String newPassword = randomString(PASSWORD_ALPHABET, PASSWORD_LENGTH);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return newPassword;
    }

    private String randomString(String alphabet, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
