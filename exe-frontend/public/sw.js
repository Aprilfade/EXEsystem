/**
 * Service Worker for Dashboard Caching
 * 实现渐进式 Web 应用的缓存策略
 */

const CACHE_NAME = 'exe-system-cache-v1';
const API_CACHE_NAME = 'exe-system-api-cache-v1';

// 需要缓存的静态资源
const STATIC_CACHE_URLS = [
  '/',
  '/index.html',
  '/manifest.json'
];

// 需要缓存的 API 接口（使用网络优先策略）
const API_CACHE_PATTERNS = [
  '/api/v1/dashboard/stats',
  '/api/v1/dashboard/todos',
  '/api/v1/dashboard/recent-activities'
];

// 缓存策略：缓存优先（Cache First）
const cacheFirst = async (request) => {
  const cache = await caches.open(CACHE_NAME);
  const cached = await cache.match(request);

  if (cached) {
    return cached;
  }

  try {
    const response = await fetch(request);
    if (response.ok) {
      cache.put(request, response.clone());
    }
    return response;
  } catch (error) {
    console.error('Fetch failed:', error);
    throw error;
  }
};

// 缓存策略：网络优先（Network First）
const networkFirst = async (request) => {
  const cache = await caches.open(API_CACHE_NAME);

  try {
    const response = await fetch(request);
    if (response.ok) {
      cache.put(request, response.clone());
    }
    return response;
  } catch (error) {
    console.error('Network request failed, trying cache:', error);
    const cached = await cache.match(request);
    if (cached) {
      return cached;
    }
    throw error;
  }
};

// 安装事件
self.addEventListener('install', (event) => {
  console.log('[ServiceWorker] Install');

  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('[ServiceWorker] Caching static assets');
      return cache.addAll(STATIC_CACHE_URLS);
    }).then(() => {
      // 立即激活新的 Service Worker
      return self.skipWaiting();
    })
  );
});

// 激活事件
self.addEventListener('activate', (event) => {
  console.log('[ServiceWorker] Activate');

  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          // 删除旧版本的缓存
          if (cacheName !== CACHE_NAME && cacheName !== API_CACHE_NAME) {
            console.log('[ServiceWorker] Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    }).then(() => {
      // 立即控制所有页面
      return self.clients.claim();
    })
  );
});

// 拦截请求
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // 只处理同源请求
  if (url.origin !== location.origin) {
    return;
  }

  // API 请求使用网络优先策略
  const isApiRequest = API_CACHE_PATTERNS.some(pattern => url.pathname.includes(pattern));

  if (isApiRequest) {
    event.respondWith(networkFirst(request));
  } else if (request.method === 'GET') {
    // 其他 GET 请求使用缓存优先策略
    event.respondWith(cacheFirst(request));
  }
});

// 消息监听（用于手动清除缓存）
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'CLEAR_CACHE') {
    event.waitUntil(
      caches.keys().then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => caches.delete(cacheName))
        );
      }).then(() => {
        console.log('[ServiceWorker] All caches cleared');
        event.ports[0].postMessage({ success: true });
      })
    );
  }

  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});
