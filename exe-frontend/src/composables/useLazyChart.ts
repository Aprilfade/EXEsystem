import { ref, onMounted, onUnmounted, type Ref } from 'vue';

/**
 * 图表懒加载 Composable
 * 使用 Intersection Observer 实现图表的按需加载
 *
 * @param chartRef 图表容器的 ref
 * @param options IntersectionObserver 配置选项
 * @returns isVisible 图表是否可见
 *
 * @example
 * const chartContainer = ref(null)
 * const { isVisible } = useLazyChart(chartContainer)
 *
 * watch(isVisible, (visible) => {
 *   if (visible) initChart()
 * })
 */
export function useLazyChart(
  chartRef: Ref<HTMLElement | null>,
  options: IntersectionObserverInit = {}
) {
  const isVisible = ref(false);
  let observer: IntersectionObserver | null = null;

  const defaultOptions: IntersectionObserverInit = {
    threshold: 0.1, // 当 10% 的元素可见时触发
    rootMargin: '50px', // 提前 50px 开始加载
    ...options
  };

  onMounted(() => {
    if (!chartRef.value) return;

    // 检查浏览器是否支持 IntersectionObserver
    if (!('IntersectionObserver' in window)) {
      // 不支持则直接显示
      isVisible.value = true;
      return;
    }

    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            // 元素进入可视区域
            isVisible.value = true;
            // 停止观察，避免重复触发
            observer?.disconnect();
          }
        });
      },
      defaultOptions
    );

    observer.observe(chartRef.value);
  });

  onUnmounted(() => {
    observer?.disconnect();
    observer = null;
  });

  return {
    isVisible
  };
}
