import requests
import random
import json
import time

# ================= 配置区域 =================
BASE_URL = "http://localhost:8080/api/v1"
ADMIN_USERNAME = "admin"  # 请确保数据库中有此管理员账号
ADMIN_PASSWORD = "password" # 请确保密码正确

# 要生成的数量
STUDENT_COUNT = 100   # 批量生成 100 个学生


# 基础数据
GRADES = ['七年级', '八年级', '九年级', '高一', '高二', '高三']
# ===========================================

class DataGenerator:
    def __init__(self):
        self.session = requests.Session()
        self.token = None
        self.headers = {'Content-Type': 'application/json'}
        self.subjects = []

    def login(self):
        """管理员登录获取 Token"""
        url = f"{BASE_URL}/auth/login"
        payload = {
            "username": ADMIN_USERNAME,
            "password": ADMIN_PASSWORD
        }
        try:
            response = self.session.post(url, json=payload)
            res_data = response.json()
            if res_data['code'] == 200:
                self.token = res_data['data']['token']
                self.headers['Authorization'] = f"Bearer {self.token}"
                print(f"✅ 登录成功，Token 获取完成")
                return True
            else:
                print(f"❌ 登录失败: {res_data['msg']}")
                return False
        except Exception as e:
            print(f"❌ 连接服务器失败: {e}")
            return False

    def get_subjects(self):
        """获取所有科目，以便关联"""
        url = f"{BASE_URL}/subjects/all" # 确保后端有这个接口，如果没有可以用分页接口
        try:
            # 如果 /all 接口不存在，尝试用分页接口获取第一页的大量数据
            response = self.session.get(url, headers=self.headers)
            if response.status_code == 404:
                response = self.session.get(f"{BASE_URL}/subjects?current=1&size=100", headers=self.headers)
                res_data = response.json()
                if res_data['code'] == 200:
                    self.subjects = res_data['data']['records']
            else:
                res_data = response.json()
                if res_data['code'] == 200:
                    self.subjects = res_data['data']

            if self.subjects:
                print(f"✅ 获取到 {len(self.subjects)} 个科目")
            else:
                print("⚠️ 未获取到科目，请先在系统中手动添加至少一个科目！")
                exit()
        except Exception as e:
            print(f"❌ 获取科目失败: {e}")
            exit()

    def create_students(self):
        """批量创建学生"""
        print(f"\n🚀 开始创建 {STUDENT_COUNT} 个学生...")
        success_count = 0
        for i in range(STUDENT_COUNT):
            # 生成随机数据
            student_no = f"STU{random.randint(100000, 999999)}"
            name = f"测试学生{i+1}"
            subject = random.choice(self.subjects)
            grade = random.choice(GRADES)

            payload = {
                "studentNo": student_no,
                "name": name,
                "password": "123", # 默认密码
                "subjectId": subject['id'],
                "grade": grade,
                "contact": f"138{random.randint(10000000, 99999999)}"
            }

            try:
                res = self.session.post(f"{BASE_URL}/students", json=payload, headers=self.headers)
                if res.json()['code'] == 200:
                    success_count += 1
                    print(f"   [{i+1}/{STUDENT_COUNT}] 创建成功: {name} ({student_no})")
                else:
                    print(f"   [{i+1}/{STUDENT_COUNT}] 创建失败: {res.json().get('msg')}")
            except Exception as e:
                print(f"   请求异常: {e}")

        print(f"🎉 学生创建完成，成功 {success_count} 个")

if __name__ == "__main__":
    print("================ 自动化数据生成脚本 ================")
    generator = DataGenerator()
    if generator.login():
        generator.get_subjects()

        # 执行生成任务
        generator.create_students()
        generator.create_questions()

    print("\n✅ 所有任务执行完毕！")