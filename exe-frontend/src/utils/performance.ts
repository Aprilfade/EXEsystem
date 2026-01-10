/**
 * 性能优化工具函数
 * Performance optimization utilities
 */

/**
 * 防抖函数 - 延迟执行,只执行最后一次
 * @param fn 要防抖的函数
 * @param delay 延迟时间(毫秒)
 * @returns 防抖后的函数
 */
export function debounce<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): (...args: Parameters<T>) => void {
  let timeoutId: ReturnType<typeof setTimeout> | null = null;

  return function(this: any, ...args: Parameters<T>) {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    timeoutId = setTimeout(() => {
      fn.apply(this, args);
      timeoutId = null;
    }, delay);
  };
}

/**
 * 节流函数 - 固定时间间隔内只执行一次
 * @param fn 要节流的函数
 * @param delay 时间间隔(毫秒)
 * @returns 节流后的函数
 */
export function throttle<T extends (...args: any[]) => any>(
  fn: T,
  delay: number = 300
): (...args: Parameters<T>) => void {
  let lastCall = 0;
  let timeoutId: ReturnType<typeof setTimeout> | null = null;

  return function(this: any, ...args: Parameters<T>) {
    const now = Date.now();
    const timeSinceLastCall = now - lastCall;

    if (timeSinceLastCall >= delay) {
      // 立即执行
      lastCall = now;
      fn.apply(this, args);
    } else {
      // 延迟执行最后一次
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
      timeoutId = setTimeout(() => {
        lastCall = Date.now();
        fn.apply(this, args);
        timeoutId = null;
      }, delay - timeSinceLastCall);
    }
  };
}

/**
 * RAF节流 - 使用requestAnimationFrame优化高频事件
 * @param fn 要节流的函数
 * @returns 节流后的函数
 */
export function rafThrottle<T extends (...args: any[]) => any>(
  fn: T
): (...args: Parameters<T>) => void {
  let rafId: number | null = null;
  let latestArgs: Parameters<T> | null = null;

  return function(this: any, ...args: Parameters<T>) {
    latestArgs = args;

    if (rafId === null) {
      rafId = requestAnimationFrame(() => {
        fn.apply(this, latestArgs!);
        rafId = null;
        latestArgs = null;
      });
    }
  };
}

/**
 * 空闲时执行 - 利用浏览器空闲时间
 * @param fn 要执行的函数
 * @param options 配置选项
 */
export function runWhenIdle(
  fn: () => void,
  options?: { timeout?: number }
): void {
  if ('requestIdleCallback' in window) {
    requestIdleCallback(fn, options);
  } else {
    // 降级方案
    setTimeout(fn, 0);
  }
}

/**
 * 图片懒加载 - IntersectionObserver
 * @param imgSelector 图片选择器
 * @param rootMargin 根边距
 */
export function lazyLoadImages(
  imgSelector: string = 'img[data-src]',
  rootMargin: string = '50px'
): void {
  if (!('IntersectionObserver' in window)) {
    // 降级方案：直接加载所有图片
    const images = document.querySelectorAll(imgSelector);
    images.forEach((img) => {
      const dataSrc = img.getAttribute('data-src');
      if (dataSrc) {
        img.setAttribute('src', dataSrc);
      }
    });
    return;
  }

  const imageObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target as HTMLImageElement;
        const dataSrc = img.getAttribute('data-src');
        if (dataSrc) {
          img.src = dataSrc;
          img.removeAttribute('data-src');
          observer.unobserve(img);
        }
      }
    });
  }, {
    rootMargin
  });

  const images = document.querySelectorAll(imgSelector);
  images.forEach(img => imageObserver.observe(img));
}

/**
 * 批量执行 - 将多个小任务合并执行
 * @param tasks 任务数组
 * @param batchSize 每批处理数量
 * @param delay 批次间延迟(毫秒)
 */
export async function batchProcess<T>(
  tasks: Array<() => Promise<T>>,
  batchSize: number = 5,
  delay: number = 10
): Promise<T[]> {
  const results: T[] = [];

  for (let i = 0; i < tasks.length; i += batchSize) {
    const batch = tasks.slice(i, i + batchSize);
    const batchResults = await Promise.all(batch.map(task => task()));
    results.push(...batchResults);

    // 批次间延迟,避免阻塞主线程
    if (i + batchSize < tasks.length) {
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }

  return results;
}

/**
 * 内存优化 - 清理缓存
 */
export function clearCache(keys: string[]): void {
  keys.forEach(key => {
    try {
      localStorage.removeItem(key);
      sessionStorage.removeItem(key);
    } catch (e) {
      console.warn(`Failed to clear cache for key: ${key}`, e);
    }
  });
}

/**
 * 性能监控 - 获取性能指标
 */
export function getPerformanceMetrics() {
  if (!('performance' in window)) {
    return null;
  }

  const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming;
  const paint = performance.getEntriesByType('paint');

  return {
    // DNS查询时间
    dnsTime: navigation.domainLookupEnd - navigation.domainLookupStart,
    // TCP连接时间
    tcpTime: navigation.connectEnd - navigation.connectStart,
    // 请求时间
    requestTime: navigation.responseEnd - navigation.requestStart,
    // 响应时间
    responseTime: navigation.responseEnd - navigation.responseStart,
    // DOM解析时间
    domParseTime: navigation.domInteractive - navigation.responseEnd,
    // 资源加载时间
    resourceTime: navigation.loadEventStart - navigation.domContentLoadedEventEnd,
    // 总加载时间
    loadTime: navigation.loadEventEnd - navigation.fetchStart,
    // 首次绘制
    firstPaint: paint.find(p => p.name === 'first-paint')?.startTime || 0,
    // 首次内容绘制
    firstContentfulPaint: paint.find(p => p.name === 'first-contentful-paint')?.startTime || 0
  };
}

/**
 * 动态导入组件 - 异步加载
 */
export function loadComponentAsync<T = any>(
  loader: () => Promise<T>,
  delay: number = 200
): Promise<T> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      loader().then(resolve).catch(reject);
    }, delay);
  });
}
