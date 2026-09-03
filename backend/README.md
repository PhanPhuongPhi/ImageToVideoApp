# ImageToVideoApp Backend API

Mã nguồn Backend phục vụ ứng dụng Android "Image To Video AI". Được xây dựng bằng Python FastAPI và SQLite.

## 1. Thành phần nộp bài
- **Mã nguồn:** `main.py`, `models.py`, `database.py`.
- **Database:** `app.db` (Tự động sinh sau khi chạy script khởi tạo).
- **Script tạo Database:** `init_db.py`.

## 2. Hướng dẫn cài đặt và chạy
1. Cài đặt Python 3.9+
2. Cài đặt các thư viện cần thiết:
   ```bash
   pip install fastapi uvicorn sqlalchemy
   ```
3. Khởi tạo Database và dữ liệu mẫu:
   ```bash
   python init_db.py
   ```
4. Chạy Server:
   ```bash
   python main.py
   ```
   Server sẽ chạy tại: `http://localhost:8000`

## 3. Tài khoản Demo
- **Admin:** `admin@gmail.com` / `admin`
- **User:** `123` / `123`

## 4. Các tính năng đã triển khai (API)
- **Auth:** Đăng ký, Đăng nhập, Lấy thông tin cá nhân.
- **Credit:** Xem số dư, Lịch sử (Mock), Mua gói Credit, Cấp Credit (Admin).
- **Video:** Gửi ảnh tạo video (Giả lập), Theo dõi trạng thái, Xem lịch sử.
- **Admin:** Thống kê Dashboard (User, Video, Doanh thu), Quản lý người dùng.
