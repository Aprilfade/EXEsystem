<template>
  <div class="login-page-wrapper">
    <div class="container" :class="{ 'right-panel-active': isSignUp }">
      <div class="container__form container--signup">
        <form class="form" @submit.prevent="handleSignUp">
          <h2 class="form__title">注 册</h2>
          <div class="input-wrap">
            <input type="text" placeholder="用户名" class="input" v-model="signUpForm.username" required />
          </div>
          <div class="input-wrap">
            <input type="text" placeholder="昵称" class="input" v-model="signUpForm.nickName" required />
          </div>
          <div class="input-wrap">
            <input type="password" placeholder="密码" class="input" v-model="signUpForm.password" required />
          </div>
          <button class="btn">注 册</button>
        </form>
      </div>

      <div class="container__form container--signin">
        <form class="form" @submit.prevent="handleSignIn">
          <h2 class="form__title">登 录</h2>
          <div class="input-wrap">
            <input type="text" placeholder="用户名" class="input" v-model="signInForm.username" required />
          </div>
          <div class="input-wrap">
            <input type="password" placeholder="密码" class="input" v-model="signInForm.password" required />
          </div>
          <a href="#" class="link">忘记密码?</a>
          <button class="btn">登 录</button>
        </form>
      </div>

      <div class="container__overlay">
        <div class="overlay">
          <div class="overlay__panel overlay--left">
            <h1 class="overlay__title">已有帐号？</h1>
            <p class="overlay__description">请使用您的帐号进行登录</p>
            <button class="btn" @click="isSignUp = false">登 录</button>
          </div>
          <div class="overlay__panel overlay--right">
            <h1 class="overlay__title">没有帐号？</h1>
            <p class="overlay__description">立即注册一个帐号，开始您的旅程</p>
            <button class="btn" @click="isSignUp = true">注 册</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
// 【新增】导入 useRoute
import { useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { ElMessage } from 'element-plus';
import { register } from '../api/auth';



const isSignUp = ref(false);
const authStore = useAuthStore();
// 【新增】获取当前路由信息
const route = useRoute();

const signInForm = reactive({
  username: 'admin',
  password: 'password',
});

const signUpForm = reactive({
  username: '',
  nickName: '',
  password: '',
});

const handleSignIn = () => {
  if (!signInForm.username || !signInForm.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  // 【修改】调用 login 时，将路由查询参数中的 redirect 传过去
  authStore.login(signInForm, route.query.redirect as string | undefined);
};
const handleSignUp = async () => {
  if (!signUpForm.username || !signUpForm.password || !signUpForm.nickName) {
    ElMessage.warning('请填写完整的注册信息');
    return;
  }
  try {
    const res = await register({
      username: signUpForm.username,
      password: signUpForm.password,
      nickName: signUpForm.nickName,
    });
    if (res.code === 200) {
      ElMessage.success(res.msg || '注册成功，请登录！');
      signUpForm.username = '';
      signUpForm.nickName = '';
      signUpForm.password = '';
      isSignUp.value = false;
    }
  } catch (error) {
    console.error('注册失败:', error);
  }
};
</script>

<style scoped>
/* 样式已根据双面板布局进行修正 */
.login-page-wrapper {
  --white: #e9e9e9;
  --gray: #333;
  --blue: #0367a6;
  --lightblue: #008997;
  --button-radius: 0.7rem;
  --max-width: 758px;
  --max-height: 480px; /* 增加高度以容纳更多内容 */

  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen,
  Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif;
  align-items: center;
  background: url("/back.jpg") center/cover no-repeat fixed; /* 使用项目内的背景图 */
  display: grid;
  height: 100vh;
  place-items: center;
}

.form__title {
  font-weight: 300;
  margin: 0 0 1.25rem;
  color: var(--gray);
  font-size: 2rem;
}

.link {
  color: var(--gray);
  font-size: 0.9rem;
  margin: 1.5rem 0;
  text-decoration: none;
}

.container {
  background-color: transparent; /* 容器本身透明 */
  border-radius: var(--button-radius);
  box-shadow: 0 0.9rem 1.7rem rgba(0, 0, 0, 0.25), 0 0.7rem 0.7rem rgba(0, 0, 0, 0.22);
  height: var(--max-height);
  max-width: var(--max-width);
  overflow: hidden;
  position: relative;
  width: 100%;
}

.container__form {
  /* ★ 核心修复：将玻璃背景应用到这里 ★ */
  background-color: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);

  height: 100%;
  position: absolute;
  top: 0;
  transition: all 0.6s ease-in-out;
}

.container--signin {
  left: 0;
  width: 50%;
  z-index: 2;
}

.container.right-panel-active .container--signin {
  transform: translateX(100%);
}

.container--signup {
  left: 0;
  opacity: 0;
  width: 50%;
  z-index: 1;
}

.container.right-panel-active .container--signup {
  animation: show 0.6s;
  opacity: 1;
  transform: translateX(100%);
  z-index: 5;
}

.container__overlay {
  height: 100%;
  left: 50%;
  overflow: hidden;
  position: absolute;
  top: 0;
  transition: transform 0.6s ease-in-out;
  width: 50%;
  z-index: 100;
}

.container.right-panel-active .container__overlay {
  transform: translateX(-100%);
}

.overlay {
  background: url("/back.jpg") center/cover no-repeat fixed;
  color: var(--white);
  height: 100%;
  left: -100%;
  position: relative;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
  width: 200%;
}

/* 覆盖层上的文字样式 */
.overlay__title {
  font-size: 2rem;
  font-weight: 500;
  margin-bottom: 1rem;
}
.overlay__description {
  font-size: 1rem;
  margin: 0 2rem 1.5rem;
}

.container.right-panel-active .overlay {
  transform: translateX(50%);
}

.overlay__panel {
  align-items: center;
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: center;
  padding: 0 40px;
  position: absolute;
  text-align: center;
  top: 0;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
  width: 50%;
}

.overlay--left {
  transform: translateX(-20%);
}

.container.right-panel-active .overlay--left {
  transform: translateX(0);
}

.overlay--right {
  right: 0;
  transform: translateX(0);
}

.container.right-panel-active .overlay--right {
  transform: translateX(20%);
}

.btn {
  background-color: var(--blue);
  background-image: linear-gradient(90deg, var(--blue) 0%, var(--lightblue) 74%);
  border-radius: 20px;
  border: 1px solid var(--blue);
  color: var(--white);
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: bold;
  letter-spacing: 0.1rem;
  padding: 0.9rem 4rem;
  text-transform: uppercase;
  transition: transform 80ms ease-in;
}

.form .btn {
  margin-top: 1.5rem;
}

.btn:active {
  transform: scale(0.95);
}

.btn:focus {
  outline: none;
}

.form {
  background-color: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 0 3rem;
  height: 100%;
  text-align: center;
}

.input-wrap {
  border: 1px solid #aaa;
  background-color: rgba(255, 255, 255, 0.5);
  margin-bottom: 1rem;
  height: 50px;
  width: 100%;
  border-radius: 50px;
}

.input {
  background-color: transparent;
  border: none;
  outline: none;
  font-size: 1rem;
  color: var(--gray);
  padding: 15px;
  width: 100%;
  height: 100%;
}
.input::placeholder {
  font-size: 1rem;
  color: #555;
}

@keyframes show {
  0%, 49.99% {
    opacity: 0;
    z-index: 1;
  }
  50%, 100% {
    opacity: 1;
    z-index: 5;
  }
}
</style>