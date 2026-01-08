import { ElMessage } from 'element-plus';

/**
 * 数据导出 Composable
 */
export function useExport() {
    /**
     * 导出为 Excel
     */
    const exportToExcel = () => {
        try {
            const url = '/api/v1/dashboard/export/excel';
            window.open(url, '_blank');
            ElMessage.success('正在导出 Excel 文件...');
        } catch (error) {
            console.error('导出 Excel 失败:', error);
            ElMessage.error('导出 Excel 失败');
        }
    };

    /**
     * 导出为图片（需要先安装 html2canvas）
     * npm install html2canvas file-saver
     */
    const exportToImage = async () => {
        try {
            // 动态导入 html2canvas 和 file-saver
            const html2canvas = (await import('html2canvas')).default;
            const { saveAs } = await import('file-saver');

            const container = document.querySelector('.dashboard-container') as HTMLElement;
            if (!container) {
                ElMessage.error('未找到工作台容器');
                return;
            }

            ElMessage.info('正在生成图片...');

            const canvas = await html2canvas(container, {
                backgroundColor: '#f5f7fa',
                scale: 2, // 提高清晰度
                logging: false,
                useCORS: true,
            });

            canvas.toBlob((blob) => {
                if (blob) {
                    const fileName = `工作台_${new Date().getTime()}.png`;
                    saveAs(blob, fileName);
                    ElMessage.success('图片导出成功');
                }
            });
        } catch (error) {
            console.error('导出图片失败:', error);
            ElMessage.error('导出图片失败，请确保已安装 html2canvas 和 file-saver');
        }
    };

    /**
     * 导出为 PDF（简化版，仅提示）
     */
    const exportToPDF = () => {
        ElMessage.info('PDF 导出功能开发中，请使用浏览器打印功能（Ctrl+P）');
        // 触发浏览器打印
        window.print();
    };

    return {
        exportToExcel,
        exportToImage,
        exportToPDF,
    };
}
