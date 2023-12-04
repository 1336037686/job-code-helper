<template>
  <el-row class="panel">
    <el-col :span="4">
      <div style="padding: 10px">
        <el-card shadow="always" class="handle-panel">
          <p>
            <el-button type="success" icon="el-icon-plus" style="width: 100%">新建聊天</el-button>
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
          <el-card shadow="always"  v-for="(item, index) in chatList" :key="index" class="chat-item" @click.native="chatClick(item.id)">
            {{ item.title }}
          </el-card>
        </div>
      </div>
    </el-col>
    <el-col :span="20">
      <div style="padding: 10px">
        <el-card shadow="always" class="message-panel">
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
              <el-button :loading="loading" icon="el-icon-delete" size="medium" type="danger"
                         @click="sendMessage"> 清空消息
              </el-button>
              <el-button :loading="loading" icon="el-icon-search" size="medium" type="success"
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
        title: null,
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
      await this.queryChat()
      if (!this.chatList || this.chatList.length === 0) {
        this.createChat()
      }
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
      qachatApi.chatUpdate(this.currChat).then(res => {
        this.currChat = res.data
        this.queryChat()
      })
    },
    async queryChat () {
      await qachatApi.chatQuery().then(res => {
        this.chatList = res.data
        if (this.chatList && this.chatList.length > 0) {
          this.currChat = this.chatList[0]
          this.queryChatById()
        }
      })
    },
    queryChatById() {
      qachatApi.chatQueryById(this.currChat.id).then(res => {
        this.messages = res.data
      })
    },
    createChat() {
      qachatApi.chatCreate({
        id: new Date().getTime(),
        title: '新建聊天',
        model: 'gpt-3.5-turbo',
        history: '2'
      }).then(res => {
        this.chatList.push(res.data)
        this.currChat = res.data
      }).catch(e => {
        console.error(e)
      })
    },

    createMessage () {
      this.form.id = this.currChat.id
      this.form.title = this.currChat.title
      this.form.mid = new Date().getTime()
      console.log(qachatApi.messageCreate(this.form.id, this.form.mid, this.form.title))
      // 在组件挂载后，创建 EventSource 对象连接到 SSE 服务器
      const eventSource = new EventSource(qachatApi.messageCreate(this.form.id, this.form.mid, this.form.title))
      // 监听 SSE 事件
      eventSource.addEventListener('message', (event) => {
        if (event.data === '[DONE]') {
          this.loading = false
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
      this.loading = true
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
        this.loading = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>

.panel {
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
