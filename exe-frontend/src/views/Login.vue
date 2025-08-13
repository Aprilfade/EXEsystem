<template>
  <div class="login-page-wrapper">
    <div class="container" :class="{ 'right-panel-active': isSignUp }">
      <div class="container__form container--signup">
        <form class="form" @submit.prevent="handleSignUp">
          <h2 class="form__title">注 册</h2>
          <input type="text" placeholder="用户名" class="input" v-model="signUpForm.username" />
          <input type="text" placeholder="昵称" class="input" v-model="signUpForm.nickName" />
          <input type="password" placeholder="密码" class="input" v-model="signUpForm.password" />
          <button class="btn">注 册</button>
        </form>
      </div>

      <div class="container__form container--signin">
        <form class="form" @submit.prevent="handleSignIn">
          <h2 class="form__title">登 录</h2>
          <input type="text" placeholder="用户名" class="input" v-model="signInForm.username" />
          <input type="password" placeholder="密码" class="input" v-model="signInForm.password" />
          <a href="#" class="link">忘记密码?</a>
          <button class="btn">登 录</button>
        </form>
      </div>

      <div class="container__overlay">
        <div class="overlay">
          <div class="overlay__panel overlay--left">
            <button class="btn" @click="isSignUp = false">登 录</button>
          </div>
          <div class="overlay__panel overlay--right">
            <button class="btn" @click="isSignUp = true">注 册</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useAuthStore } from '../stores/auth';
import { ElMessage } from 'element-plus';
import { register } from '../api/auth';

// 控制切换动画的响应式变量
const isSignUp = ref(false);

// 获取 Pinia store
const authStore = useAuthStore();

// 登录表单数据
const signInForm = reactive({
  username: 'admin', // 预填数据方便测试
  password: 'password',
});

// 【修改】注册表单数据，与后端接口对应
const signUpForm = reactive({
  username: '',
  nickName: '',
  password: '',
});
/**
 * @description 处理登录逻辑
 * 调用 authStore 中的 login action
 */
const handleSignIn = () => {
  if (!signInForm.username || !signInForm.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  authStore.login(signInForm);
};


/**
 * 【修改】实现完整的注册逻辑
 */
const handleSignUp = async () => {
  if (!signUpForm.username || !signUpForm.password || !signUpForm.nickName) {
    ElMessage.warning('请填写完整的注册信息');
    return;
  }
  try {
    const res = await register({
      username: signUpForm.username,
      password: signUpForm.password,
      // 后端 SysUser 需要 nickName 字段
      nickName: signUpForm.nickName,
    });
    // 后端返回的 Result 对象中 code 为 200 代表成功
    if (res.code === 200) {
      ElMessage.success(res.msg || '注册成功，请登录！');
      // 注册成功后，清空表单并自动切换回登录界面
      signUpForm.username = '';
      signUpForm.nickName = '';
      signUpForm.password = '';
      isSignUp.value = false;
    }
  } catch (error) {
    // API 请求本身失败或返回错误状态码时，错误会被 request.ts 拦截并提示
    console.error('注册失败:', error);
  }
};

</script>

<style scoped>
/* 这里是所有的新样式 */
.login-page-wrapper {
  /* COLORS */
  --white: #e9e9e9;
  --gray: #333;
  --blue: #0367a6;
  --lightblue: #008997;
  /* RADII */
  --button-radius: 0.7rem;
  /* SIZES */
  --max-width: 758px;
  --max-height: 420px;

  font-size: 16px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen,
  Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif;

  align-items: center;
  background-color: var(--white);
  /* 使用你项目中的背景图 */
  background: url("/login-bg.png");
  background-attachment: fixed;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  display: grid;
  height: 100vh;
  place-items: center;
}

.form__title {
  font-weight: 300;
  margin: 0;
  margin-bottom: 1.25rem;
}

.link {
  color: var(--gray);
  font-size: 0.9rem;
  margin: 1.5rem 0;
  text-decoration: none;
}

.container {
  background-color: var(--white);
  border-radius: var(--button-radius);
  box-shadow: 0 0.9rem 1.7rem rgba(0, 0, 0, 0.25),
  0 0.7rem 0.7rem rgba(0, 0, 0, 0.22);
  height: var(--max-height);
  max-width: var(--max-width);
  overflow: hidden;
  position: relative;
  width: 100%;
}

.container__form {
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
  background-color: var(--lightblue);
  background: url("/login-bg.png");
  background-attachment: fixed;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  height: 100%;
  left: -100%;
  position: relative;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
  width: 200%;
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

.form > .btn {
  margin-top: 1.5rem;
}

.btn:active {
  transform: scale(0.95);
}

.btn:focus {
  outline: none;
}

.form {
  background-color: var(--white);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 0 3rem;
  height: 100%;
  text-align: center;
}

.input {
  background-color: #eee;
  border: none;
  padding: 0.9rem 0.9rem;
  margin: 0.5rem 0;
  width: 100%;
  border-radius: 0.5rem;
}

@keyframes show {
  0%,
  49.99% {
    opacity: 0;
    z-index: 1;
  }

  50%,
  100% {
    opacity: 1;
    z-index: 5;
  }
}
</style>