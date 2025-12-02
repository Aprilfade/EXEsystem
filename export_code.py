import os

# 配置需要扫描的目录
TARGET_DIRS = [
    os.path.join('exe-backend', 'src', 'main', 'java'), # 后端代码
    os.path.join('exe-frontend', 'src')                 # 前端代码
]

# 配置需要提取的文件后缀
EXTENSIONS = ['.java', '.ts', '.vue']

# 输出文件
OUTPUT_FILE = 'source_code_for_copyright.txt'

def is_comment(line):
    """简单的注释判断"""
    stripped = line.strip()
    # 单行注释
    if stripped.startswith('//'):
        return True
    # 块注释开头/结尾 (简单处理，不完美但够用)
    if stripped.startswith('/*') or stripped.startswith('*') or stripped.endswith('*/'):
        return True
    return False

def extract_code():
    total_lines = 0
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as outfile:
        for target_dir in TARGET_DIRS:
            for root, dirs, files in os.walk(target_dir):
                for file in files:
                    if any(file.endswith(ext) for ext in EXTENSIONS):
                        file_path = os.path.join(root, file)
                        try:
                            with open(file_path, 'r', encoding='utf-8') as infile:
                                lines = infile.readlines()
                                for line in lines:
                                    # 去除空行
                                    if not line.strip():
                                        continue
                                    # 去除简单注释 (可选，软著建议去除注释以增加有效代码密度)
                                    if is_comment(line):
                                        continue

                                    outfile.write(line)
                                    total_lines += 1
                        except Exception as e:
                            print(f"Skipping file {file}: {e}")

    print(f"代码提取完成！已生成: {OUTPUT_FILE}")
    print(f"总行数约为: {total_lines}")

if __name__ == '__main__':
    extract_code()