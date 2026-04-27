// ==================== 全局变量 ====================
let pendingClassification = null;
let messageInput;
let sendBtn;
let messageList;
let currentChatTitle;

// 新增：会话本地存储（刷新不丢失）
let chatList = [];
let currentChatId = "default";

window.onload = function () {
    messageInput = document.getElementById('messageInput');
    sendBtn = document.getElementById('sendBtn');
    messageList = document.getElementById('messageList');
    currentChatTitle = document.getElementById('currentChatTitle');

    // 加载本地会话
    loadLocalChats();

    // 绑定原有快捷按钮
    document.querySelectorAll('.quick-btn').forEach(btn => {
        const action = btn.dataset.action;
        if (action === '1' || action === '2' || action === '3' || action === '4') {
            btn.onclick = () => fillQuickQuestion(Number(action));
        } else if (action === 'loadUnclassified') {
            btn.onclick = loadUnclassifiedOrders;
        } else if (action === 'goToClassification') {
            btn.onclick = goToClassification;
        }
    });

    sendBtn.onclick = sendMessage;
    document.querySelector('.new-chat-btn').onclick = createNewChat;

    // 会话切换 + 删除（修复事件绑定）
    document.getElementById('chatList').addEventListener('click', function (e) {
        const item = e.target.closest('.chat-item');
        if (!item) return;

        // 删除会话
        if (e.target.classList.contains('delete-chat')) {
            deleteChat(item.dataset.chatId);
            return;
        }

        // 切换会话
        switchChat(item.dataset.chatId);
    });

    // ==============================================
    // 🔥 在这里集成：回车发送消息（禁止换行）
    // ==============================================
    messageInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
};

// ==================== 本地存储（新增）====================
function saveLocalChats() {
    localStorage.setItem("ai_chat_history", JSON.stringify(chatList));
}

function loadLocalChats() {
    const saved = localStorage.getItem("ai_chat_history");
    if (saved) {
        chatList = JSON.parse(saved);
    } else {
        chatList = [{ id: "default", title: "默认会话", messages: [] }];
    }
    renderChatList();
    switchChat(currentChatId);
}

function renderChatList() {
    const container = document.getElementById("chatList");
    container.innerHTML = "";
    chatList.forEach(chat => {
        const div = document.createElement("div");
        div.className = `chat-item ${chat.id === currentChatId ? "active" : ""}`;
        div.dataset.chatId = chat.id;
        div.innerHTML = `
            <span class="chat-title">${chat.title}</span>
            <span class="delete-chat">×</span>
        `;
        container.appendChild(div);
    });
}

function switchChat(chatId) {
    currentChatId = chatId;
    const chat = chatList.find(c => c.id === chatId);
    currentChatTitle.textContent = chat.title;
    renderMessages(chat.messages);
    renderChatList();
}

// ✅【修复】新建会话（真正添加到左侧列表）
function createNewChat() {
    const newId = "chat_" + Date.now();
    chatList.push({
        id: newId,
        title: `新会话`,
        messages: []
    });
    saveLocalChats();
    renderChatList();
    switchChat(newId);
}

// ✅【修复】删除会话（无报错版）
function deleteChat(chatId) {
    if (chatList.length <= 1) {
        alert("至少保留一个会话");
        return;
    }
    chatList = chatList.filter(c => c.id !== chatId);
    if (currentChatId === chatId) {
        currentChatId = chatList[0].id;
    }
    saveLocalChats();
    renderChatList();
    switchChat(currentChatId);
}

// ✅【修复】保存消息 + 自动设置第一条消息为标题
function saveCurrentMessage(role, content) {
    const chat = chatList.find(c => c.id === currentChatId);
    chat.messages.push({ role, content });

    // 第一条用户消息 → 自动变成会话标题
    if (chat.messages.length === 1 && role === "user") {
        let title = content.length > 18 ? content.substring(0, 18) + "..." : content;
        chat.title = title;
        currentChatTitle.textContent = title;
    }

    saveLocalChats();
    renderChatList();
}

function renderMessages(msgs) {
    messageList.innerHTML = "";
    if (msgs.length === 0) {
        messageList.innerHTML = `<div class="empty-tip">开始和AI对话吧～</div>`;
        return;
    }
    msgs.forEach(({ role, content }) => {
        addMessageToUI(role, content, false);
    });
}

// ==================== 工具方法（你原有代码）====================
function disableInput() {
    messageInput.disabled = true;
    sendBtn.disabled = true;
}

function enableInput() {
    messageInput.disabled = false;
    sendBtn.disabled = false;
}

function removeEmptyTip() {
    const emptyTip = document.querySelector('.empty-tip');
    if (emptyTip) emptyTip.remove();
}

function scrollToBottom() {
    messageList.scrollTop = messageList.scrollHeight;
}

function addLoadingState() {
    const loadingId = 'loading-' + Date.now();
    const html = `
        <div class="message-item ai-message" id="${loadingId}">
            <div class="message-content">
                <div class="loading">
                    <span class="loading-dot"></span>
                    <span class="loading-dot"></span>
                    <span class="loading-dot"></span>
                </div>
            </div>
        </div>
    `;
    messageList.insertAdjacentHTML('beforeend', html);
    scrollToBottom();
    return loadingId;
}

function removeLoadingState(loadingId) {
    const el = document.getElementById(loadingId);
    if (el) el.remove();
}

// ==================== 消息显示（你原有 + 本地存储）====================
function addMessageToUI(role, content, autoScroll = true) {
    removeEmptyTip();
    const className = role === 'user' ? 'user-message' : 'ai-message';
    const html = `
        <div class="message-item ${className}">
            <div class="message-content">${content.replace(/\n/g, '<br>')}</div>
        </div>
    `;
    messageList.insertAdjacentHTML('beforeend', html);
    if (autoScroll) scrollToBottom();
}

// ==================== 发送消息（你原有逻辑 + 本地保存）====================
async function sendMessage() {
    const message = messageInput.value.trim();
    if (!message) {
        alert('请输入要发送的消息！');
        return;
    }

    disableInput();
    messageInput.value = '';
    addMessageToUI('user', message);
    saveCurrentMessage("user", message);

    const loadingId = addLoadingState();

    try {
        // 调用AI接口处理消息
        const response = await fetch('/ai/ticket', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: message })
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const result = await response.json();
        removeLoadingState(loadingId);
        
        console.log('后端返回数据:', result);  // 调试日志

        // 处理响应
        if (result.code === 200 && result.data) {
            // 检查是否需要确认
            if (result.data.needConfirm) {
                // 显示分类预览并添加确认按钮
                displayClassificationPreview(result.data);
            } else if (result.data.orderInfo) {
                // 工单详情查询结果
                const orderInfo = result.data.orderInfo;
                const reply = `📋 工单详情\n\n工单号：${orderInfo.orderId}\n内容：${orderInfo.orderContent}\n时间：${orderInfo.acceptTime || '未记录'}`;
                addMessageToUI('ai', reply);
                saveCurrentMessage("ai", reply);
            } else {
                // 其他数据直接显示
                const reply = typeof result.data === 'string' ? result.data : JSON.stringify(result.data, null, 2);
                addMessageToUI('ai', reply);
                saveCurrentMessage("ai", reply);
            }
        } else if (result.status === 'success' || result.status === 'warning') {
            // 处理特殊状态
            const reply = result.message || '操作成功';
            addMessageToUI('ai', reply);
            saveCurrentMessage("ai", reply);
        } else {
            // 错误消息
            const reply = `❌ ${result.message || '操作失败'}`;
            addMessageToUI('ai', reply);
            saveCurrentMessage("ai", reply);
        }
    } catch (error) {
        removeLoadingState(loadingId);
        console.error('发送消息失败:', error);
        console.error('错误堆栈:', error.stack);
        const errorMsg = `❌ 网络异常: ${error.message || '请稍后重试'}`;
        addMessageToUI('ai', errorMsg);
        saveCurrentMessage("ai", errorMsg);
    } finally {
        enableInput();
    }
}

// ==================== 显示分类预览（带确认按钮）====================
function displayClassificationPreview(data) {
    const orderInfo = data.orderInfo;
    const classification = data.classification;
    const confirmHint = data.confirmHint || '请确认是否进行分类？';
    
    // 安全地获取confidence值
    const confidence = classification.confidence || 0;
    const confidencePercent = (confidence * 100).toFixed(1);
    
    let reply = `🏷️ 分类预览结果\n\n`;
    reply += `━━━━━━━━━━━━━━━\n`;
    reply += `📋 工单号：${orderInfo.orderId}\n`;
    reply += `📝 工单内容：${orderInfo.orderContent || '无内容'}\n`;
    reply += `━━━━━━━━━━━━━━━\n\n`;
    reply += `✅ 推荐分类：${classification.categoryName || '未分类'}\n`;
    reply += `📊 分类ID：${classification.categoryId || 'N/A'}\n`;
    reply += `📈 置信度：${confidencePercent}%\n`;
    reply += `🔍 匹配类型：${classification.matchType || 'N/A'}\n`;
    if (classification.matchEvidence) {
        reply += `💡 匹配证据：${classification.matchEvidence}\n`;
    }
    if (classification.aiReasoning) {
        reply += `🤖 AI推理：${classification.aiReasoning}\n`;
    }
    reply += `\n━━━━━━━━━━━━━━━\n`;
    reply += `💡 ${confirmHint}`;
    
    addMessageToUI('ai', reply);
    saveCurrentMessage("ai", reply);
    
    // 添加确认和取消按钮
    const buttonsHtml = `
        <div style="margin-top: 10px; text-align: center;">
            <button class="confirm-btn" onclick="confirmOrderClassification(${orderInfo.orderId}, ${classification.categoryId})" style="margin-right: 10px; padding: 8px 20px; background: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer;">
                ✓ 确认分类
            </button>
            <button class="cancel-btn" onclick="skipClassification(${orderInfo.orderId})" style="padding: 8px 20px; background: #f44336; color: white; border: none; border-radius: 4px; cursor: pointer;">
                ✗ 跳过分类
            </button>
        </div>
    `;
    messageList.lastElementChild.innerHTML += buttonsHtml;
    scrollToBottom();
}

// ==================== 确认工单分类 ====================
async function confirmOrderClassification(orderId, categoryId) {
    disableInput();
    addMessageToUI('user', `确认分类：工单 ${orderId} -> 分类 ${categoryId}`);
    const lid = addLoadingState();

    try {
        const response = await fetch('/ai/ticket', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: `确认 ${orderId} ${categoryId}` })
        });
        
        const result = await response.json();
        removeLoadingState(lid);

        if (result.code === 200 || result.status === 'success') {
            const reply = `✅ 分类确认成功\n\n工单号：${orderId}\n已分类为：分类ID ${categoryId}\n\n${result.message || ''}`;
            addMessageToUI('ai', reply);
            saveCurrentMessage("ai", reply);
        } else {
            const reply = `❌ 分类确认失败：${result.message || '未知错误'}`;
            addMessageToUI('ai', reply);
            saveCurrentMessage("ai", reply);
        }
    } catch (error) {
        removeLoadingState(lid);
        addMessageToUI('ai', '❌ 网络异常，请稍后重试');
        saveCurrentMessage("ai", '❌ 网络异常，请稍后重试');
    } finally {
        enableInput();
    }
}

// ==================== 跳过分类 ====================
async function skipClassification(orderId) {
    disableInput();
    addMessageToUI('user', `跳过分类：工单 ${orderId}`);
    const lid = addLoadingState();

    try {
        const response = await fetch('/ai/ticket', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: `跳过 ${orderId}` })
        });
        
        const result = await response.json();
        removeLoadingState(lid);

        if (result.code === 200 || result.status === 'success') {
            const reply = `⏭️ 已跳过分类\n\n工单号：${orderId}\n\n${result.message || ''}`;
            addMessageToUI('ai', reply);
            saveCurrentMessage("ai", reply);
        } else {
            const reply = `❌ 操作失败：${result.message || '未知错误'}`;
            addMessageToUI('ai', reply);
            saveCurrentMessage("ai", reply);
        }
    } catch (error) {
        removeLoadingState(lid);
        addMessageToUI('ai', '❌ 网络异常，请稍后重试');
        saveCurrentMessage("ai", '❌ 网络异常，请稍后重试');
    } finally {
        enableInput();
    }
}

// ==================== 核心功能 ====================
async function handleAICommand(orderId) {
    disableInput();
    addMessageToUI('user', `查询工单号 ${orderId} 详情`);
    const lid = addLoadingState();

    try {
        const res = await fetch(`/api/classify/order-detail/${orderId}`);
        const result = await res.json();
        removeLoadingState(lid);

        if (result.code === 200 && result.data) {
            const o = result.data;
            addMessageToUI('ai', `📋 工单详情\n工单号：${o.orderId}\n内容：${o.orderContent}`);
            addMessageToUI('ai', '是否对该工单分类？');
            messageList.lastElementChild.innerHTML += `
                <div style="margin-top:8px">
                    <button class="confirm-btn" onclick="confirmClassify(${o.orderId})">✓ 确认分类</button>
                    <button class="cancel-btn" onclick="cancelOperation()">取消</button>
                </div>
            `;
        } else {
            addMessageToUI('ai', '❌ 工单不存在');
        }
    } catch (e) {
        removeLoadingState(lid);
        addMessageToUI('ai', '❌ 网络异常');
    } finally {
        enableInput();
    }
}

async function queryWorkOrder(id) {
    return fetch(`/api/classify/order-detail/${id}`);
}

async function previewClassify(content) {
    return fetch('/api/classify/preview', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ orderContent: content })
    });
}

async function callGeneralApi(msg) {
    return fetch('/api/ai/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: msg })
    });
}

function detectActionType(text) {
    text = text.trim().toLowerCase();
    if (/^\d+$/.test(text)) return 'query';
    if (['分类', '归类', '内容'].some(w => text.includes(w))) return 'classify';
    if (['查询', '工单', 'id', '详情'].some(w => text.includes(w))) return 'query';
    return 'chat';
}

function extractClassificationContent(message) {
    const prefixes = ['请帮我分类以下工单内容：', '分类内容：', '工单内容：', '帮我分类：'];
    for (const p of prefixes) {
        if (message.startsWith(p)) return message.substring(p.length).trim();
    }
    return message;
}

function formatQueryResponse(data) {
    return `📋 工单信息\n工单号：${data.orderId}\n内容：${data.orderContent}`;
}

// ==================== 按钮方法 ====================

function fillQuickQuestion(type) {
    // 类型3是统计功能，直接调用
    if (type === 3) {
        loadStatistics();
        return;
    }
    
    // 类型4是示例工单，直接调用分类
    if (type === 4) {
        loadExampleTicket();
        return;
    }
    
    const map = {
        1: '请帮我分类以下工单内容：',
        2: '查询工单号：'
    };
    messageInput.value = map[type] || '';
    messageInput.focus();
}

function goToClassification() {
    window.location.href = 'classification.html';
}

async function loadUnclassifiedOrders() {
    disableInput();
    addMessageToUI('user', '📋 查看未分类工单');
    const lid = addLoadingState();

    try {
        const res = await fetch('/api/classify/unclassified');
        const data = await res.json();
        removeLoadingState(lid);

        if (data.code === 200 && data.data && data.data.length > 0) {
            let text = `📋 未分类工单列表\n\n`;
            text += `共找到 ${data.data.length} 个未分类工单：\n`;
            text += `━━━━━━━━━━━━━━━\n`;
            
            data.data.forEach((o, index) => {
                text += `\n【${index + 1}】工单号：${o.orderId}\n`;
                text += `   内容：${o.orderContent}\n`;
                if (o.acceptTime) {
                    text += `   时间：${o.acceptTime}\n`;
                }
            });
            
            text += `\n━━━━━━━━━━━━━━━`;
            text += `\n💡 提示：输入"分类 工单号"即可对工单进行分类`;
            
            addMessageToUI('ai', text);
            saveCurrentMessage("ai", text);
        } else {
            const noDataMsg = '✅ 暂无未分类工单\n\n所有工单已完成分类，太棒了！';
            addMessageToUI('ai', noDataMsg);
            saveCurrentMessage("ai", noDataMsg);
        }
    } catch (e) {
        removeLoadingState(lid);
        const errorMsg = '❌ 网络异常，无法获取未分类工单列表';
        addMessageToUI('ai', errorMsg);
        saveCurrentMessage("ai", errorMsg);
    } finally {
        enableInput();
    }
}

async function confirmClassify(orderId) {
    disableInput();
    const lid = addLoadingState();
    try {
        const res = await fetch(`/api/classify/classify/${orderId}`);
        const d = await res.json();
        removeLoadingState(lid);
        addMessageToUI('ai', d.code === 200 ? '✅ 分类完成' : '❌ 分类失败');
    } catch (e) {
        removeLoadingState(lid);
        addMessageToUI('ai', '❌ 网络异常');
    } finally {
        enableInput();
    }
}

function cancelOperation() {
    enableInput();
    addMessageToUI('ai', '✅ 已取消操作');
}

// ==================== 示例工单功能 ====================
async function loadExampleTicket() {
    disableInput();
    const exampleContent = '用户反馈楼下垃圾堆积，臭味严重，希望尽快处理';
    addMessageToUI('user', `📝 示例工单：${exampleContent}`);
    const lid = addLoadingState();

    try {
        const res = await fetch('/api/classify/preview', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ orderContent: exampleContent })
        });
        const result = await res.json();
        removeLoadingState(lid);

        if (result.code === 200 && result.data) {
            const data = result.data;
            const reply = `🏷️ 工单分类预览

📝 工单内容：
${exampleContent}

━━━━━━━━━━━━━━━

✅ 推荐分类：${data.categoryName || '未分类'}
📊 分类代码：${data.categoryCode || 'N/A'}
📈 置信度：${data.confidence ? (data.confidence * 100).toFixed(1) + '%' : '0.0%'}
🔍 匹配类型：${data.matchType || 'N/A'}
💡 匹配证据：${data.matchEvidence || '无'}

━━━━━━━━━━━━━━━

💡 提示：这是一个示例工单的分类预览结果`;
            
            addMessageToUI('ai', reply);
            saveCurrentMessage("ai", reply);
        } else {
            const errorMsg = `❌ 分类失败: ${result.message || '未知错误'}`;
            addMessageToUI('ai', errorMsg);
            saveCurrentMessage("ai", errorMsg);
        }
    } catch (e) {
        removeLoadingState(lid);
        const errorMsg = '❌ 网络异常，无法获取分类结果';
        addMessageToUI('ai', errorMsg);
        saveCurrentMessage("ai", errorMsg);
    } finally {
        enableInput();
    }
}

// ==================== 统计信息功能 ====================
async function loadStatistics() {
    disableInput();
    addMessageToUI('user', '📊 查看系统统计信息');
    const lid = addLoadingState();

    try {
        const res = await fetch('/api/classify/stats/data');
        const result = await res.json();
        removeLoadingState(lid);

        if (result.code === 200 && result.data) {
            const stats = result.data;
            const classifiedRate = stats.totalOrders > 0 
                ? ((stats.classifiedOrders / stats.totalOrders) * 100).toFixed(1) 
                : 0;
            
            const reply = `📊 系统统计信息

📈 工单统计
━━━━━━━━━━━━━━━
📦 总工单数：${stats.totalOrders}
✅ 已分类工单：${stats.classifiedOrders}
⏳ 未分类工单：${stats.unclassifiedOrders}
📊 分类率：${classifiedRate}%

🏷️ 分类统计
━━━━━━━━━━━━━━━
📋 分类总数：${stats.totalCategories}

📝 规则统计
━━━━━━━━━━━━━━━
🔢 关键词规则数：${stats.totalKeywords}`;
            
            addMessageToUI('ai', reply);
            saveCurrentMessage("ai", reply);
        } else {
            const errorMsg = `❌ 获取统计信息失败: ${result.message || '未知错误'}`;
            addMessageToUI('ai', errorMsg);
            saveCurrentMessage("ai", errorMsg);
        }
    } catch (e) {
        removeLoadingState(lid);
        const errorMsg = '❌ 网络异常，无法获取统计信息';
        addMessageToUI('ai', errorMsg);
        saveCurrentMessage("ai", errorMsg);
    } finally {
        enableInput();
    }
}