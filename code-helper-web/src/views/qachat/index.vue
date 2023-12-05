<template>
  <el-row
    class="panel"
    v-loading="loading"
    element-loading-text="加载中，请稍后..."
    element-loading-spinner="el-icon-loading"
  >
    <el-col :span="4">
      <div style="padding: 10px">
        <el-card shadow="hover" class="handle-panel">
          <p>
            <el-button type="success" icon="el-icon-plus" style="width: 100%" @click="chatCreate">新建聊天</el-button>
          </p>
          <p>
            <el-select v-model="currChat.model" style="width: 100%;" placeholder="请选择使用模型" @change="chatChange">
              <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </p>
        </el-card>
        <div class="chat-list-panel">
          <el-card shadow="hover"  v-for="(item, index) in chatList" :key="index" class="chat-item" @click.native="chatClick(item.id)">
            <span class="chat-item-truncate-text">{{ item.title }}</span>
            <el-popconfirm
              icon="el-icon-info"
              icon-color="red"
              title="确定删除该聊天吗？"
              @onConfirm="chatDelete(item.id)"
            >
              <i slot="reference" class="el-icon-delete chat-item-edit"></i>
            </el-popconfirm>

            <el-popover
              placement="bottom"
              width="200"
              trigger="click"
              v-model="item.visible">
              <p>
                <el-input v-model="item.tmpTitle" placeholder="请输入聊天标题" style="width: 100%;margin-bottom: 10px"></el-input>
              </p>
              <div style="text-align: right; margin: 0">
                <el-button size="mini" type="text" @click="item.visible = false">取消</el-button>
                <el-button type="primary" size="mini" @click="chatUpdate(item)">确定</el-button>
              </div>
              <i slot="reference" class="el-icon-edit chat-item-edit" @click.native="editClick(item)"></i>
            </el-popover>
          </el-card>
        </div>
      </div>
    </el-col>
    <el-col :span="20">
      <div style="padding: 10px">
        <el-card shadow="hover" class="message-panel">
          <el-container style="height: 88vh">
            <el-header class="centered-header">AI Chat</el-header>
            <el-main ref="scrollContainer" style="padding: 10px; overflow-y: auto;height: 50vh">
              <div v-for="(message, index) in messages" :key="index">
                <div
                  :class="{'user-role': message.role === 'user', 'agent-role': message.role !== 'user', 'message-role': true}">
                  {{ message.role === 'user' ? '🌾 You：' : '🌳 AI System：' }}
                </div>
                <div :class="{ 'user-message': message.role === 'user', 'agent-message': message.role !== 'user' }">
                  <vue-markdown class="message-content markdown-body" :source="message.content" v-highlight></vue-markdown>
                </div>
              </div>
            </el-main>
            <el-row>
              <el-input v-model="form.message" :autosize="{ minRows: 3, maxRows: 3 }" placeholder="输入你的问题..." size="medium"
                        style="width: 100%;" type="textarea"/>
            </el-row>
            <el-row style="text-align: right;margin-top: 5px">
              <el-select v-model="currChat.history" placeholder="携带历史记录数" style="margin-right: 10px" @change="chatChange">
                <el-option
                  v-for="item in historyOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
              <el-button :loading="clearBtnLoading" icon="el-icon-delete" size="medium" type="danger"
                         @click="chatClear"> 清空消息
              </el-button>
              <el-button :loading="btnLoading" icon="el-icon-search" size="medium" type="success"
                         @click="sendMessage"> 发送消息
              </el-button>
            </el-row>

          </el-container>
        </el-card>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import VueMarkdown from 'vue-markdown'
import qachatApi from '@/api/qachat-api'
import fa from 'element-ui/src/locale/lang/fa'

export default {
  components: {
    VueMarkdown
  },
  data () {
    return {
      options: [
        {
          value: 'gpt-3.5-turbo',
          label: 'GPT-3.5-TURBO'
        },
        {
          value: 'gpt-3.5-turbo-16k',
          label: 'GPT-3.5-TURBO-16K'
        },
        {
          value: 'gpt-4',
          label: 'GPT-4'
        },
      ],
      historyOptions: [
        {
          value: '1',
          label: '不关联历史记录'
        },
        {
          value: '2',
          label: '关联最新5条记录'
        },
        {
          value: '3',
          label: '关联最新10条记录'
        },
        {
          value: '4',
          label: '关联全部记录'
        }
      ],
      messages: [],
      clearBtnLoading: false,
      btnLoading: false,
      loading: false,
      currChat: {
        id: null,
        title: null,
        model: 'gpt-3.5-turbo',
        history: '2'
      },
      chatList: [],
      form: {
        id: null,
        mid: null,
        message: null
      }
    }
  },
  mounted () {
    this.init()
  },
  methods: {
    async init () {
      this.loading = true
      await this.queryChat()
      if (!this.chatList || this.chatList.length === 0) {
        this.createChat()
      }
    },
    async chatCreate () {
      await this.createChat()
    },
    editClick(item) {
      item.tmpTitle = item.title
      item.visible = true
    },
    chatUpdate(item) {
      this.loading = true
      let tmpItem = this.deepClone(item)
      tmpItem.title = tmpItem.tmpTitle
      qachatApi.chatUpdate(tmpItem).then(res => {
        this.$message({
          message: '更新成功',
          type: 'success'
        });
        item.visible = false
        this.loading = false
        this.queryChat()
      })
    },
    chatDelete(id) {
      this.loading = true
      qachatApi.chatDelete(id).then(res => {
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        this.loading = false
        this.queryChat()
      })
    },
    chatClick(chatId) {
      console.log(chatId)
      if (this.chatList && this.chatList.length > 0) {
        for (let i = 0; i < this.chatList.length; i++) {
          if (this.chatList[i].id === chatId) {
            this.currChat = this.chatList[i]
            this.queryChatById()
            break
          }
        }
      }
    },
    chatChange() {
      this.loading = true
      qachatApi.chatUpdate(this.currChat).then(res => {
        this.currChat = res.data
        this.loading = false
        this.queryChat()
      })
    },
    chatClear() {
      this.clearBtnLoading = true
      qachatApi.chatDelete(this.currChat.id).then(res => {
        this.clearBtnLoading = false
        this.$message({
          message: '清空成功',
          type: 'success'
        })
        this.queryChatById()
      })
    },
    async queryChat () {
      this.loading = true
      await qachatApi.chatQuery().then(res => {
        this.loading = false
        this.chatList = res.data
        if (this.chatList && this.chatList.length > 0) {
          this.currChat = this.chatList[0]
          this.queryChatById()
        }
      })
    },
    queryChatById() {
      this.loading = true
      qachatApi.chatQueryById(this.currChat.id).then(res => {
        this.loading = false
        this.messages = res.data
      })
    },
    createChat() {
      this.loading = true
      qachatApi.chatCreate({
        id: new Date().getTime(),
        title: '新建聊天',
        model: 'gpt-3.5-turbo',
        history: '2'
      }).then(res => {
        this.loading = false
        this.queryChat()
      }).catch(e => {
        console.error(e)
      })
    },
    createMessage () {
      this.form.id = this.currChat.id
      this.form.mid = new Date().getTime()
      console.log(qachatApi.messageCreate(this.form.id, this.form.mid))
      // 在组件挂载后，创建 EventSource 对象连接到 SSE 服务器
      const eventSource = new EventSource(qachatApi.messageCreate(this.form.id, this.form.mid))
      // 监听 SSE 事件
      eventSource.addEventListener('message', (event) => {
        if (event.data === '[DONE]') {
          this.btnLoading = false
          return
        }
        const eventData = JSON.parse(event.data)
        // 在收到消息时更新组件的数据
        if (eventData.role) {
          const mesObj = {role: eventData.role, content: eventData.content}
          this.messages.push(mesObj)
          return
        }
        // 拼接返回消息
        if (!eventData.content) return
        this.messages[this.messages.length - 1].content += eventData.content
      })
      // 监听 SSE 错误事件
      eventSource.addEventListener('error', (error) => {
        console.error('SSE Error:', error)
        // 在发生错误时可以执行一些处理逻辑
      })
      // 在组件销毁时关闭 SSE 连接
      this.$once('hook:beforeDestroy', () => {
        eventSource.close()
      })
    },
    async sendMessage () {
      this.btnLoading = true
      await this.createMessage()

      let isReadyStatus = false
      while (isReadyStatus === false) {
        await qachatApi.messageIsReady(this.form).then(res => {
          isReadyStatus = res.data
        })
        if (isReadyStatus === false) {
          // 暂停500ms
          await new Promise(resolve => setTimeout(resolve, 500))
        }
      }
      this.messages.push({ role: 'user', content: this.form.message })
      await qachatApi.messageSend(this.form).then(res => {
        this.form.message = null
      }).catch(e => {
        this.btnLoading = false
      })
    },
    deepClone(obj) {
      if (obj === null || typeof obj !== 'object') {
        return obj
      }
      let copy = Array.isArray(obj) ? [] : {}
      for (let key in obj) {
        if (obj.hasOwnProperty(key)) {
          copy[key] = this.deepClone(obj[key])
        }
      }
      return copy
    }
  }
}
</script>

<style lang="scss" scoped>

.panel {
  margin-top: 10px;
  background-color: white;
}

.handle-panel {
  background-color: white;
}

.chat-list-panel {
  height: 73vh;
  overflow-y: auto;
  margin-top: 10px;
}

/* 使用CSS选择器选择要应用样式的元素 */
.chat-item-truncate-text {
  white-space: nowrap;         /* 禁止文字换行 */
  overflow: hidden;            /* 隐藏溢出的部分 */
  text-overflow: ellipsis;     /* 使用省略号表示溢出的部分 */
  max-width: 180px;            /* 设置最大宽度，根据需要调整 */
  display: inline-block;       /* 使得元素表现为内联块级元素，以便宽度生效 */
}

.chat-item-edit {
  float: right;
  font-size: 16px;
  margin-left: 5px;
}

.chat-item-edit:hover {
  cursor: pointer; /* 设置鼠标移入时的样式为手型 */
}

.chat-item {
  margin-bottom: 10px;
  background-color: #fff;
  color: #43454a;
}

.message-panel {
  background-color: white;
}

.input-container {
  margin-top: 10px;
  background-color: #fff;
}

.centered-header {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #6bb283;
  color: white;
  padding: 10px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  border-radius: 10px;
}

.message-role {
  font-weight: bold;
  //color: white;
}

.user-role {
  text-align: right;
}

.agent-role {
  text-align: left;
}

.user-message {
  text-align: right;
  margin-bottom: 10px;
}

.agent-message {
  text-align: left;
  margin-bottom: 10px;
}

.user-message .message-content, .agent-message .message-content {
  margin-top: 10px;
  padding-left: 15px;
  padding-right: 15px;
  border-radius: 10px;
  display: inline-block;
  max-width: 70%; /* Adjust as needed */
}

.user-message .message-content {
  background-color: #88b373; /* User message background color */
  text-align: left;
  color: white;
}

.agent-message .message-content {
  background-color: #E1EBE3; /* User message background color */
  text-align: left;
  color: #364050;
}

.markdown-body {
  box-sizing: border-box;
  padding: 20px;
}

/* 隐藏浏览器默认滚动条 */
body {
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
}

/* Webkit浏览器滚动条样式 */
::-webkit-scrollbar {
  width: 5px;
}

::-webkit-scrollbar-thumb {
  background-color: #888; /* 滚动条拖动部分的颜色 */
  border-radius: 6px; /* 圆角 */
}

::-webkit-scrollbar-track {
  background-color: #f5f5f5; /* 滚动条轨道的颜色 */
  border-radius: 6px; /* 圆角 */
}
</style>
