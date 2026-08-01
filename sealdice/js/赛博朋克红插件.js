// ==UserScript==
// @name         赛博朋克红核心规则插件
// @author       Fehling0v0
// @version      1.0.1
// @description  指令：.cpr  .dv  .ri  .cprst .set cpr
// @timestamp    1785586743
// 2026/8/1
// @license      Apache-2
// @homepageURL  https://raw.githubusercontent.com/Fehling0v0/TRPG/refs/heads/main/sealdice/js/%E8%B5%9B%E5%8D%9A%E6%9C%8B%E5%85%8B%E7%BA%A2%E6%8F%92%E4%BB%B6.js
// @updateUrl    https://github.com/Fehling0v0/TRPG/blob/main/sealdice/js
// ==/UserScript==


const cprTemplate = {
    name:'cpr',
    fullName: '赛博朋克红核心规则插件',
    authors: ['Fehling0v0'],
    version: '1.0.1',
    updatedTime: '',
    templateVer: '1.0.1',
    //.set 相关内容，使用.set cpr开启，切10面骰，并提示enableTip中的内容
    setConfig:{
        diceSides: 10,
        enableTip: '已切换至10面骰，并自动开启赛博朋克红扩展',
        keys: ['cpr', '赛博朋克红'],
        relatedExt: ['cpr', 'coc7'], // 开启coc7是为了蹭coc7的st指令
    },
    // sn相关内容，可使用.sn cpr自动设置名片
    nameTemplate:{
        cpr:{
            template: '{$t玩家_RAW} HP{生命值}/{生命值上限} SP{身体SP}',
            helpText: '自动设置赛博朋克红名片',
        }
    },
    attrConfig: {
        // st show 置顶内容
        top: ["智力", "反应", "敏捷", "技术", "酷", "意志", "幸运", "移动", "体魄","共情","HP","人性"],
        // st show 隐藏内容
        ignores:["cprst_custom_map","生命上限","生命"],
        // st show 展示内容，例如到 st show hp 会展示“生命值: 10/14”
        showAs: {
            "HP": "{生命}/{生命上限}",
            //"EMP": "{当前共情}/{共情}",
            //"HUM": "{人性}/{人性上限}",
        }
    },
    defaultsComputed: {
        "生命上限": "10 + 5 * Math.ceil((体魄 + 意志) / 2)",
        //"人性上限": "共情 * 10",
        //"人性上限": "共情 * 10",
    },
    //同义词 这翻译五花八门的我已经看晕了，凑合用吧
    alias: {
        "智力": ["int", "INT"],
        "反应": ["ref", "REF"],
        "敏捷": ["dex", "DEX"],
        "技术": ["tech", "TECH"],
        "酷": ["cool", "COOL"],
        "意志": ["will", "WILL"],
        "幸运": ["luck", "LUCK"],
        "体魄": ["body", "BODY"],
        "共情": ["emp", "EMP"],
        "移动": ["move", "MOVE"],
        "生命": ["hp", "HP"],
        "人性": ["hum", "HUM"],
        "专注": ["专注力"],
        "觉察": ["察觉"],
        "交流": ["交谈"],
        "抵抗拷问/药物": ["折磨/药物耐受", "药物耐受", "折磨耐受", "抵抗拷问", "抵抗药物","折磨药物耐受","抵抗拷问药物"],
        "藏匿/搜寻物品": ["藏匿/发现物品", "藏匿物品", "搜寻物品", "发现物品"],
        "街头黑话": ["街头俚语"],
        "官僚世故": ["官场世故"],
        "驾驶地面载具": ["地面载具驾驶"],
        "驾驶空中载具": ["空中载具驾驶"],
        "驾驶海上载具": ["海上载具驾驶"],
        "会计": ["会计学"],
        "商业": ["商学"],
        "创作": ["写作"],
        "教育": ["通识"],
        "图书馆检索": ["图书馆", "图书馆使用"],
        "徒手搏斗": ["搏击"],
        "箭术": ["弓术"],
        "重型武器": ["重武器"],
        "抵肩枪械": ["长枪射击"],
        "个个人仪容": ["人装扮"],
        "衣着与风格": ["服饰与风格"],
        "爆破": ["爆破学"],
        "表演": ["扮演"],
        "绘画/素描/雕塑": ["绘画/雕刻/速写", "绘画", "素描", "雕塑", "雕刻", "速写","绘画素描雕塑","绘画雕刻速写"],
        "摄影/影片": ["摄影/摄像", "摄影", "摄像", "影片","摄影影片","摄影摄像"],
        "开锁": ["撬锁", "锁匠"],
        "水上载具技术": ["海上载具技术", "海洋载具技术"],
        "电子/安防技术": ["电子/安全技术","电子技术","安防技术","安全技术","电子安全","电子安防","电子安全技术","电子安防技术"],
        "身体SP": ["sp", "SP", "躯干SP","身体sp","躯干sp","身体护甲","躯干护甲","躯干护甲SP","身体护甲SP","躯干护甲sp","身体护甲sp"],
        "头部SP": ["头部sp","头部护甲","头部护甲SP","头部护甲sp"],
        "护盾SP": ["护盾sp","护盾护甲","护盾护甲SP","护盾护甲sp"],
    },

}
//预设卡模板
const cprCharacterTemplates = {
    "摇滚小子": [
        [7, 6, 6, 5, 6, 8, 7, 7, 3, 8],
        [3, 7, 7, 7, 7, 6, 7, 7, 5, 8],
        [4, 5, 7, 7, 6, 6, 7, 7, 5, 8],
        [4, 5, 7, 7, 6, 8, 7, 6, 3, 8],
        [3, 7, 7, 7, 6, 8, 6, 5, 4, 7],
        [5, 6, 7, 5, 7, 8, 5, 7, 3, 7],
        [5, 6, 6, 7, 7, 8, 7, 6, 3, 6],
        [5, 7, 7, 5, 6, 6, 6, 6, 4, 8],
        [3, 5, 5, 6, 7, 8, 7, 5, 5, 7],
        [4, 5, 6, 5, 8, 8, 7, 6, 4, 7]
    ],
    "佣兵": [
        [6, 7, 7, 3, 8, 6, 5, 5, 6, 5],
        [7, 8, 6, 3, 6, 6, 7, 5, 6, 6],
        [5, 8, 7, 4, 7, 7, 6, 7, 8, 5],
        [5, 8, 6, 4, 6, 7, 6, 5, 7, 6],
        [6, 6, 7, 5, 7, 6, 7, 6, 8, 4],
        [7, 7, 6, 5, 7, 6, 6, 7, 7, 5],
        [7, 7, 6, 5, 6, 7, 7, 6, 6, 6],
        [7, 8, 7, 5, 6, 6, 5, 6, 8, 4],
        [7, 7, 6, 4, 6, 6, 6, 5, 6, 5],
        [6, 6, 8, 5, 6, 6, 5, 6, 6, 5]
    ],
    "网行者": [
        [5, 8, 7, 7, 7, 4, 8, 7, 7, 4],
        [5, 6, 7, 5, 8, 3, 8, 7, 5, 5],
        [5, 6, 8, 6, 6, 4, 7, 6, 7, 4],
        [5, 7, 7, 7, 7, 5, 8, 6, 5, 5],
        [5, 8, 8, 5, 7, 3, 7, 5, 5, 6],
        [6, 6, 6, 7, 8, 4, 7, 7, 6, 6],
        [6, 6, 6, 7, 6, 5, 7, 7, 7, 6],
        [5, 7, 8, 6, 8, 4, 8, 5, 7, 4],
        [7, 6, 7, 7, 6, 3, 6, 5, 6, 5],
        [7, 8, 6, 6, 6, 4, 7, 7, 5, 6]
    ],
    "技术": [
        [6, 7, 7, 8, 4, 4, 5, 5, 7, 6],
        [7, 6, 6, 7, 5, 3, 7, 7, 5, 5],
        [8, 6, 5, 7, 5, 4, 7, 7, 5, 7],
        [7, 8, 7, 8, 4, 4, 6, 5, 6, 7],
        [6, 6, 7, 6, 4, 3, 7, 7, 6, 6],
        [8, 7, 5, 6, 3, 3, 7, 6, 6, 7],
        [8, 6, 7, 8, 4, 4, 7, 6, 7, 6],
        [8, 8, 7, 8, 5, 4, 6, 5, 6, 6],
        [6, 6, 7, 8, 3, 3, 5, 7, 7, 7],
        [8, 8, 5, 6, 4, 4, 6, 5, 6, 6]
    ],
    "技医": [
        [7, 5, 6, 7, 5, 3, 8, 5, 5, 7],
        [6, 7, 7, 7, 4, 4, 6, 7, 7, 7],
        [6, 5, 5, 8, 5, 3, 8, 5, 7, 8],
        [8, 7, 6, 8, 3, 5, 6, 6, 5, 7],
        [6, 7, 5, 7, 5, 5, 8, 7, 6, 8],
        [8, 5, 5, 8, 5, 5, 6, 6, 5, 6],
        [8, 6, 5, 8, 5, 4, 8, 5, 7, 7],
        [6, 5, 7, 7, 3, 5, 8, 5, 5, 8],
        [6, 6, 7, 7, 5, 4, 6, 6, 5, 6],
        [8, 7, 6, 6, 3, 4, 8, 7, 6, 7]
    ],
    "媒体人": [
        [6, 6, 5, 5, 8, 7, 5, 7, 5, 7],
        [8, 7, 7, 3, 6, 6, 6, 5, 6, 8],
        [6, 7, 7, 5, 6, 8, 5, 5, 5, 7],
        [6, 5, 7, 5, 6, 7, 5, 5, 6, 6],
        [6, 6, 7, 4, 8, 7, 6, 7, 5, 8],
        [7, 5, 5, 4, 8, 7, 6, 7, 5, 8],
        [8, 5, 6, 3, 7, 6, 6, 5, 6, 7],
        [6, 5, 6, 5, 6, 8, 6, 6, 7, 8],
        [7, 7, 5, 4, 6, 7, 6, 5, 6, 7],
        [7, 6, 6, 3, 7, 6, 7, 6, 7, 6]
    ],
    "执法者": [
        [5, 6, 7, 5, 7, 8, 5, 6, 5, 6],
        [6, 6, 6, 5, 6, 8, 5, 7, 5, 5],
        [5, 7, 7, 7, 6, 7, 5, 5, 7, 6],
        [6, 6, 7, 6, 6, 8, 5, 7, 7, 6],
        [6, 6, 7, 6, 7, 7, 6, 5, 5, 6],
        [7, 6, 5, 5, 7, 8, 5, 6, 7, 4],
        [7, 8, 7, 5, 6, 8, 7, 6, 5, 4],
        [5, 6, 6, 5, 6, 8, 5, 7, 6, 4],
        [7, 7, 5, 5, 7, 7, 6, 5, 5, 6],
        [6, 6, 5, 6, 8, 7, 5, 7, 6, 6]
    ],
    "主管": [
        [8, 5, 5, 3, 8, 6, 6, 5, 5, 7],
        [8, 6, 6, 4, 7, 6, 7, 7, 5, 7],
        [8, 7, 6, 3, 8, 6, 7, 6, 4, 5],
        [8, 5, 7, 5, 6, 5, 6, 5, 5, 7],
        [7, 7, 6, 5, 8, 5, 7, 7, 5, 6],
        [5, 7, 7, 3, 6, 7, 6, 5, 5, 7],
        [6, 6, 7, 5, 8, 7, 6, 7, 4, 6],
        [6, 7, 7, 3, 7, 5, 7, 5, 5, 7],
        [7, 6, 7, 5, 7, 5, 7, 6, 5, 5],
        [7, 7, 5, 5, 8, 6, 6, 7, 4, 7]
    ],
    "中间人": [
        [8, 5, 7, 4, 6, 5, 8, 5, 5, 8],
        [8, 5, 5, 5, 6, 7, 8, 7, 5, 7],
        [6, 6, 6, 4, 5, 6, 8, 6, 3, 8],
        [7, 7, 5, 5, 7, 6, 7, 7, 5, 8],
        [8, 6, 6, 3, 6, 5, 8, 7, 5, 6],
        [8, 7, 5, 5, 6, 7, 7, 5, 3, 6],
        [8, 6, 6, 5, 6, 5, 6, 7, 5, 8],
        [6, 6, 7, 4, 7, 6, 7, 7, 4, 7],
        [8, 7, 7, 5, 5, 5, 7, 6, 5, 7],
        [6, 5, 6, 5, 5, 6, 8, 6, 4, 7]
    ],
    "游民": [
        [6, 6, 8, 3, 6, 7, 6, 6, 6, 4],
        [5, 7, 6, 5, 8, 8, 8, 7, 5, 4],
        [5, 8, 6, 3, 8, 7, 6, 5, 6, 5],
        [5, 8, 7, 4, 8, 6, 7, 7, 7, 5],
        [6, 6, 6, 3, 6, 7, 6, 7, 7, 4],
        [7, 6, 8, 4, 6, 7, 6, 5, 6, 5],
        [6, 7, 8, 4, 6, 6, 7, 5, 7, 5],
        [5, 7, 8, 3, 8, 6, 7, 5, 5, 5],
        [6, 7, 6, 4, 8, 6, 6, 6, 6, 6],
        [5, 6, 7, 4, 7, 8, 7, 7, 7, 4]
    ]
};
//技能属性映射
const skillAttrMap = {
    "专注": "意志",
    "藏匿/发现物品": "智力",
    "唇语": "智力",
    "觉察": "智力",
    "追踪": "智力",
    "运动": "敏捷",
    "柔术": "敏捷",
    "舞蹈": "敏捷",
    "忍耐": "意志",
    "抵抗拷问/药物": "意志",
    "潜行": "敏捷",
    "驾驶地面载具": "反应",
    "驾驶空中载具": "反应",
    "驾驶海上载具": "反应",
    "骑乘": "反应",
    "会计": "智力",
    "驯兽": "智力",
    "官僚世故": "智力",
    "商业": "智力",
    "创作": "智力",
    "犯罪学": "智力",
    "密码学": "智力",
    "推理": "智力",
    "教育": "智力",
    "赌博": "智力",
    "街头黑话": "智力",
    "母语": "智力",
    "图书馆检索": "智力",
    "战术": "智力",
    "野外生存": "智力",
    "本地专家": "智力",
    "徒手搏斗": "敏捷",
    "闪避": "敏捷",
    "武术": "敏捷",
    "近战武器": "敏捷",
    "表演": "酷",
    "乐器演奏": "技术",
    "箭术": "反应",
    "自动武器": "反应",
    "手枪": "反应",
    "重武器": "反应",
    "抵肩枪械": "反应",
    "贿赂": "酷",
    "交流": "共情",
    "察言观色": "共情",
    "审讯": "酷",
    "说服": "酷",
    "个人仪容": "酷",
    "街头智慧": "酷",
    "交易": "酷",
    "衣着与风格": "酷",
    "空中载具技术": "技术",
    "基础技术": "技术",
    "赛博技术": "技术",
    "爆破学": "技术",
    "电子/安防技术": "技术",
    "急救": "技术",
    "医疗": "技术",
    "伪造": "技术",
    "地面载具技术": "技术",
    "绘画/素描/雕塑": "技术",
    "摄影/影片": "技术",
    "开锁": "技术",
    "扒窃": "技术",
    "水上载具技术": "技术",
    "武器技术": "技术"
};

const attrKeys = ["智力", "反应", "敏捷", "技术", "酷", "意志", "幸运", "移动", "体魄", "共情"];
try {
    seal.gameSystem.newTemplate(JSON.stringify(cprTemplate));
} catch (e) {
    // 如果扩展已存在，或加载失败，那么会走到这里
    console.log(e);
}

let ext = seal.ext.find('cpr');
if (!ext) {
    ext = seal.ext.new('cpr', 'Fehling0v0', '1.0.0');
    seal.ext.register(ext);
}

const cmdCpr = seal.ext.newCmdItemInfo();
cmdCpr.name = 'cpr'; 
cmdCpr.help = '赛博朋克红规则插件\n'+
'切换规则：.set cpr\n'+
'制卡：\n'+
'.cpr 街头鼠辈 【职业】：以街头鼠辈方式生成一组属性\n'+
'.cpr 边缘行者 【职业】：以边缘行者方式生成一组属性\n'+
'技能检定：\n'+
'.dv 【难度值（可省略）】 【技能】 【修正（可省略，带+-符号）】\n'+
'先攻：.ri\n'+
'技能属性映射自定义：\n'+
'.cprst 【技能】 【属性】：设置技能与属性的自定义映射\n'+
'.cprst del 【技能】：删除技能的自定义映射\n'+
'.cprst list：列出所有自定义映射\n'+
'.cprst clear：清空所有自定义映射\n'+
'\n注意：\n本插件预制了一定的同义词适配，但由于翻译版本不同，可能存在未加入的同义词，导致技能属性自动映射失败，可以使用自定义映射的方式解决。\n'+
"共情属性只会在被使用在检定中时更新。如：当人性由41降到39时，st show共情仍显示4，但是当进行交流检定时，参与计算的共情会更新为3，此时再次st show共情显示为3。";
cmdCpr.solve = (ctx, msg, cmdArgs) => {
    let val = cmdArgs.getArgN(1);
    if (!val) {
        const ret = seal.ext.newCmdExecuteResult(true);
        ret.showHelp = true;
        return ret;
    }
    switch (val) {
        case '街头鼠辈':{
            let job = cmdArgs.getArgN(2);
            if (!job) {
                let jobs = Object.keys(cprCharacterTemplates).join('、');
                seal.replyToSender(ctx, msg, `请指定职业，可用职业：${jobs}`);
                return;
            }
            let template = cprCharacterTemplates[job];
            if (!template) {
                let jobs = Object.keys(cprCharacterTemplates).join('、');
                seal.replyToSender(ctx, msg, `职业「${job}」不存在，可用职业：${jobs}`);
                return;
            }
            let roll = Math.floor(Math.random() * 10);
            let attrs = template[roll];
            for (let i = 0; i < attrKeys.length; i++) {
                seal.vars.intSet(ctx, attrKeys[i], attrs[i]);
            }
            let body = attrs[8];
            let will = attrs[5];
            let hp = 10 + 5 * Math.ceil((body + will) / 2);
            seal.vars.intSet(ctx, "生命", hp);
            seal.vars.intSet(ctx, "生命上限", hp);
            let hum = attrs[9] * 10;
            seal.vars.intSet(ctx, "人性", hum);
            seal.vars.intSet(ctx, "人性上限", hum);
            const playerName = seal.format(ctx, "{$t玩家}");
            let reply = `${playerName}的赛博朋克红人物作成（1d10=${roll + 1}）:\n`;
            reply += `智力:${attrs[0]}   反应:${attrs[1]}   敏捷:${attrs[2]}\n`;
            reply += `技术:${attrs[3]}   意志:${attrs[5]}   酷:${attrs[4]}\n`;
            reply += `幸运:${attrs[6]}   移动:${attrs[7]}   体魄:${attrs[8]}\n`;
            reply += `共情:${attrs[9]}   HP:${hp}   人性:${hum}`;   
            seal.replyToSender(ctx, msg, reply);
            break;
        }
        case '边缘行者':{
            let job = cmdArgs.getArgN(2);
            if (!job) {
                let jobs = Object.keys(cprCharacterTemplates).join('、');
                seal.replyToSender(ctx, msg, `请指定职业，可用职业：${jobs}`);
                return;
            }
            let template = cprCharacterTemplates[job];
            if (!template) {
                let jobs = Object.keys(cprCharacterTemplates).join('、');
                seal.replyToSender(ctx, msg, `职业「${job}」不存在，可用职业：${jobs}`);
                return;
            }
            let finalAttrs = [];
            for (let i = 0; i < attrKeys.length; i++) {
                let roll = Math.floor(Math.random() * 10);
                let tableValue = template[roll][i];
                let finalValue = Math.max(roll + 1, tableValue);
                finalAttrs.push(finalValue);
                seal.vars.intSet(ctx, attrKeys[i], finalValue);
            }
            let body = finalAttrs[8];
            let will = finalAttrs[5];
            let hp = 10 + 5 * Math.ceil((body + will) / 2);
            seal.vars.intSet(ctx, "生命", hp);
            seal.vars.intSet(ctx, "生命上限", hp);
            let hum = finalAttrs[9] * 10;
            seal.vars.intSet(ctx, "人性", hum);
            seal.vars.intSet(ctx, "人性上限", hum);
            const playerName = seal.format(ctx, "{$t玩家}");
            let reply = `${playerName}的赛博朋克红人物作成:\n`;
            reply += `智力:${finalAttrs[0]}   反应:${finalAttrs[1]}   敏捷:${finalAttrs[2]}\n`;
            reply += `技术:${finalAttrs[3]}   意志:${finalAttrs[5]}   酷:${finalAttrs[4]}\n`;  
            reply += `幸运:${finalAttrs[6]}   移动:${finalAttrs[7]}   体魄:${finalAttrs[8]}\n`;
            reply += `共情:${finalAttrs[9]}   HP:${hp}   人性:${hum}`;   
            seal.replyToSender(ctx, msg, reply);
            break;
        }
    }
}

function getCustomSkillMap(ctx) {
    let [json] = seal.vars.strGet(ctx, 'cprst_custom_map');
    if (!json) return {};
    try {
        return JSON.parse(json);
    } catch (e) {
        return {};
    }
}

function setCustomSkillMap(ctx, map) {
    seal.vars.strSet(ctx, 'cprst_custom_map', JSON.stringify(map));
}

function getSkillGroup(skill) {
    for (let mainName in cprTemplate.alias) {
        if (mainName === skill || cprTemplate.alias[mainName].includes(skill)) {
            return [mainName, ...cprTemplate.alias[mainName]];
        }
    }
    return [skill];
}

function findAttrMapping(skill, customMap) {
    let group = getSkillGroup(skill);
    for (let name of group) {
        if (customMap[name]) {
            return customMap[name];
        }
    }
    for (let name of group) {
        if (skillAttrMap[name]) {
            return skillAttrMap[name];
        }
    }
    return null;
}

function doSkillCheck(ctx, skill, modifier) {
    let customMap = getCustomSkillMap(ctx);
    let attrName = findAttrMapping(skill, customMap);
    let attrVal = 0;
    if (attrName) {
        if (attrName === '共情') {
            let [hum] = seal.vars.intGet(ctx, '人性');
            if (hum !== null) {
                let newEmp = Math.floor(hum / 10);
                seal.vars.intSet(ctx, '共情', newEmp);
            }
        }
        let [val] = seal.vars.intGet(ctx, attrName);
        if (val !== null) attrVal = val;
    }
    let [skillVal] = seal.vars.intGet(ctx, skill);
    if (skillVal === null) skillVal = 0;
    let roll1 = Math.floor(Math.random() * 10) + 1;
    let total = attrVal + skillVal + roll1 + modifier;
    let roll2 = 0;
    let special = "";
    if (roll1 === 10) {
        roll2 = Math.floor(Math.random() * 10) + 1;
        total += roll2;
        special = ` 大成功]+${roll2}[1d10`;
    } else if (roll1 === 1) {
        roll2 = Math.floor(Math.random() * 10) + 1;
        total -= roll2;
        special = ` 大失败]-${roll2}[1d10`;
    }
    return { attrName, attrVal, skillVal, roll1, roll2, total, modifier, special };
}


const cmdDv = seal.ext.newCmdItemInfo();
cmdDv.name = 'dv'; 
cmdDv.help = '赛博朋克红技能检定\n'+
'.dv 【难度值（可省略）】 【技能】 【修正（可省略，带+-符号）】：进行技能检定\n';
cmdDv.solve = (ctx, msg, cmdArgs) => {
    let arg1 = cmdArgs.getArgN(1);
    let arg2 = cmdArgs.getArgN(2);
    let arg3 = cmdArgs.getArgN(3);
    if (!arg1) {
        const ret = seal.ext.newCmdExecuteResult(true);
        ret.showHelp = true;
        return ret;
    }
    let rounds = 1;
    let dv = NaN;
    let skill, modifier;
    let currentArg = arg1;
    let argIndex = 1;
    if (currentArg.includes('#')) {
        let roundMatch = currentArg.match(/^(\d+)#/);
        if (roundMatch) {
            rounds = parseInt(roundMatch[1]);
            currentArg = currentArg.replace(/^\d+#\s*/, '');
        }
    }
    if (!currentArg) {
        currentArg = arg2;
        argIndex = 2;
    }
    let parsedDv = parseInt(currentArg);
    if (!isNaN(parsedDv)) {
        dv = parsedDv;
        skill = cmdArgs.getArgN(argIndex + 1);
        modifier = 0;
        let modArg = cmdArgs.getArgN(argIndex + 2);
        if (modArg) {
            let match = modArg.match(/^[+-]\d+$/);
            if (match) {
                modifier = parseInt(modArg);
            }
        }
    } else {
        skill = currentArg;
        modifier = 0;
        let modArg = cmdArgs.getArgN(argIndex + 1);
        if (modArg) {
            let match = modArg.match(/^[+-]\d+$/);
            if (match) {
                modifier = parseInt(modArg);
            }
        }
    }
    if (!skill) {
        const ret = seal.ext.newCmdExecuteResult(true);
        ret.showHelp = true;
        return ret;
    }
    if (rounds > 20) {
        let warning = seal.formatTmpl(ctx, "COC:检定_轮数过多警告");
        seal.replyToSender(ctx, msg, warning);
        return seal.ext.newCmdExecuteResult(true);
    }
    if (rounds <= 1) {
        let checkResult = doSkillCheck(ctx, skill, modifier);
        if (checkResult.error) {
            seal.replyToSender(ctx, msg, checkResult.error);
            return seal.ext.newCmdExecuteResult(true);
        }
        let resultext = "";
        let finalresultext = "";
        seal.vars.strSet(ctx,'$t属性表达式文本',skill);
        let modifierText = checkResult.modifier !== 0 ? `${checkResult.modifier > 0 ? '+' : ''}${checkResult.modifier}` : '';
        if (checkResult.attrName) {
            resultext += `${checkResult.total}=${checkResult.skillVal}[${skill}]+${checkResult.attrVal}[${checkResult.attrName}]+${checkResult.roll1}[1d10${checkResult.special}]${modifierText}`;
        } else {
            resultext += `${checkResult.total}=${checkResult.skillVal}[${skill}]+${checkResult.roll1}[1d10${checkResult.special}]${modifierText}`;
        }
        seal.vars.strSet(ctx,'$t结果文本',resultext);
        if (!isNaN(dv)) {
            seal.vars.strSet(ctx,'$t原因',`dv${dv}`);
            finalresultext += seal.formatTmpl(ctx, "COC:检定");
            if (checkResult.total > dv) {
                if (checkResult.roll2 > 0) {
                    finalresultext += seal.formatTmpl(ctx, "COC:判定_大成功");
                } else {
                    finalresultext += seal.formatTmpl(ctx, "COC:判定_成功_普通");
                }
            } else {
                if (checkResult.roll2 > 0) {
                    finalresultext += seal.formatTmpl(ctx, "COC:判定_大失败");
                } else {
                    finalresultext += seal.formatTmpl(ctx, "COC:判定_失败");
                }
            }
        } else {
            finalresultext += seal.formatTmpl(ctx, "COC:检定");
        }
        seal.replyToSender(ctx, msg, finalresultext);
    } else {
        seal.vars.strSet(ctx,'$t次数', rounds.toString());
        seal.vars.strSet(ctx,'$t属性表达式文本', skill);
        let resultText = "";
        for (let i = 0; i < rounds; i++) {
            let checkResult = doSkillCheck(ctx, skill, modifier);
            if (checkResult.error) {
                seal.replyToSender(ctx, msg, checkResult.error);
                return seal.ext.newCmdExecuteResult(true);
            }
            let modifierText = checkResult.modifier !== 0 ? `${checkResult.modifier > 0 ? '+' : ''}${checkResult.modifier}` : '';
            let resultext = "";
            if (checkResult.attrName) {
                resultext += `${checkResult.total}=${checkResult.skillVal}[${skill}]+${checkResult.attrVal}[${checkResult.attrName}]+${checkResult.roll1}[1d10${checkResult.special}]${modifierText}`;
            } else {
                resultext += `${checkResult.total}=${checkResult.skillVal}[${skill}]+${checkResult.roll1}[1d10${checkResult.special}]${modifierText}`;
            }
            resultText += resultext;
            if (!isNaN(dv)) {
                if (checkResult.total > dv) {
                    if (checkResult.roll2 > 0) {
                        resultText += seal.formatTmpl(ctx, "COC:判定_简短_大成功");
                    } else {
                        resultText += seal.formatTmpl(ctx, "COC:判定_简短_成功_普通");
                    }
                } else {
                    if (checkResult.roll2 > 0) {
                        resultText += seal.formatTmpl(ctx, "COC:判定_简短_大失败");
                    } else {
                        resultText += seal.formatTmpl(ctx, "COC:判定_简短_失败");
                    }
                }
            }
            if (i < rounds - 1) {
                resultText += "\n";
            }
        }
        seal.vars.strSet(ctx,'$t结果文本', resultText);
        if (!isNaN(dv)) {
            seal.vars.strSet(ctx,'$t原因',`dv${dv}`);
        }
        let finalresultext = seal.formatTmpl(ctx, "COC:检定");
        seal.replyToSender(ctx, msg, finalresultext);
    }
}




const cmdCprst = seal.ext.newCmdItemInfo();
cmdCprst.name = 'cprst'; 
cmdCprst.help = '赛博朋克红技能属性映射设置\n'+
'.cprst 【技能】 【属性】：设置技能与属性的自定义映射\n'+
'.cprst del 【技能】：删除技能的自定义映射\n'+
'.cprst list：列出所有自定义映射\n'+
'.cprst clear：清空所有自定义映射\n';
cmdCprst.solve = (ctx, msg, cmdArgs) => {
    let arg1 = cmdArgs.getArgN(1);
    let arg2 = cmdArgs.getArgN(2);
    if (!arg1) {
        const ret = seal.ext.newCmdExecuteResult(true);
        ret.showHelp = true;
        return ret;
    }
    let customMap = getCustomSkillMap(ctx);
    if (arg1 === 'del') {
        let skill = arg2;
        if (!skill) {
            seal.replyToSender(ctx, msg, '用法：.cprst del 【技能】');
            return seal.ext.newCmdExecuteResult(true);
        }
        if (customMap[skill]) {
            delete customMap[skill];
            setCustomSkillMap(ctx, customMap);
            seal.replyToSender(ctx, msg, `已删除：${skill}`);
        } else {
            seal.replyToSender(ctx, msg, `未找到技能「${skill}」的自定义映射`);
        }
        return seal.ext.newCmdExecuteResult(true);
    } else if (arg1 === 'list') {
        if (Object.keys(customMap).length === 0) {
            seal.replyToSender(ctx, msg, '暂无自定义映射');
        } else {
            let list = Object.entries(customMap).map(([skill, attr]) => `${skill} → ${attr}`).join('\n');
            seal.replyToSender(ctx, msg, `自定义技能映射：\n${list}`);
        }
        return seal.ext.newCmdExecuteResult(true);
    } else if (arg1 === 'clear') {
        setCustomSkillMap(ctx, {});
        seal.replyToSender(ctx, msg, '已清空所有自定义映射');
        return seal.ext.newCmdExecuteResult(true);
    } else {
        let skill = arg1;
        let attr = arg2;
        if (!attr) {
            seal.replyToSender(ctx, msg, '用法：.cprst 【技能】 【属性】');
            return seal.ext.newCmdExecuteResult(true);
        }
        customMap[skill] = attr;
        setCustomSkillMap(ctx, customMap);
        seal.replyToSender(ctx, msg, `已设置：${skill} → ${attr}`);
        return seal.ext.newCmdExecuteResult(true);
    }
}

const cmdRi = seal.ext.newCmdItemInfo();
cmdRi.name = 'ri'; 
cmdRi.solve = (ctx, msg, cmdArgs) => {
    let [refVal] = seal.vars.intGet(ctx, "反应");
    if (refVal === null) refVal = 0;
    let roll = Math.floor(Math.random() * 10) + 1;
    let total = refVal + roll;
    seal.vars.strSet(ctx,'$t属性表达式文本',"先攻值");
    let reply = "";
    reply += `${total}=${refVal}[反应]+${roll}[1d10]`;
    seal.vars.strSet(ctx,'$t结果文本',reply);
    reply = seal.formatTmpl(ctx, "COC:检定");
    seal.replyToSender(ctx, msg, reply);
    return seal.ext.newCmdExecuteResult(true);
}

ext.cmdMap["cpr"] = cmdCpr;
ext.cmdMap["dv"] = cmdDv;
ext.cmdMap["cprst"] = cmdCprst;
ext.cmdMap["ri"] = cmdRi;
