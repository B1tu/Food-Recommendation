# Food Recommendation System

Đồ án web gợi ý món ăn và nhà hàng theo sở thích, chế độ ăn và ngân sách của người dùng, có thêm chatbot AI (Gemini) để hỏi đáp trực tiếp thay vì phải bấm lọc thủ công. Backend viết bằng Java Spring Boot, frontend bằng React (Vite), dữ liệu lưu PostgreSQL.

## Tính năng

- Đăng ký / đăng nhập, có chế độ dùng thử (guest) không cần tạo tài khoản
- Tạo hồ sơ người dùng: sở thích ăn uống, chế độ ăn, ngân sách
- Gợi ý món ăn dựa trên hồ sơ + lịch sử đã dùng (chấm điểm bằng RecommendationEngine)
- Gợi ý nhà hàng gần vị trí người dùng hoặc theo quận/khu vực
- Chatbot AI (gọi Gemini API) trả lời câu hỏi, gợi ý món/nhà hàng kèm thẻ thông tin, nhớ ngữ cảnh trong phiên chat
- Xem thông tin dinh dưỡng từng món
- Lưu lịch sử món đã chọn

## Cài đặt

### Yêu cầu

- JDK 21
- Node.js >= 20
- PostgreSQL 13+

Không cần cài Maven riêng, project có sẵn `mvnw` / `mvnw.cmd`.

### Các bước

Clone/giải nén project, tạo database Postgres tên `foodrecommendationdb`. Lưu ý `ddl-auto=none` nên Spring không tự tạo bảng, database phải có sẵn schema trước.

Repo có kèm `data/seed.sql` — chạy 1 lần trên database vừa tạo (còn trống) để có sẵn bảng + data mẫu cho `food`, `restaurant`, `nutrition`, `restaurant_food` (85 món, 47 nhà hàng), test được luôn phần gợi ý món/nhà hàng/chatbot:
```
psql -U postgres -d foodrecommendationdb -f data/seed.sql
```
File này chỉ tạo được 1 lần trên db trống (chạy lại lần 2 sẽ báo lỗi "already exists" vì bảng đã có). Các bảng còn lại (`users`, `user_profile`, `history`, `recommendation`, `AI_CONVERSATION`) chưa có trong seed này — cần đăng ký tài khoản thử qua app để tự sinh dữ liệu, hoặc tạo schema riêng cho các bảng đó.

Cài dependency backend:
```
cd FoodRecommendationSystem
./mvnw dependency:go-offline
```

Cài dependency frontend:
```
cd frontend
npm install
```

## Cách sử dụng

Set biến môi trường trước khi chạy backend (bắt buộc phải có `GEMINI_API_KEY`, lấy ở Google AI Studio):
```
export GEMINI_API_KEY=xxx
```

Chạy backend (cổng mặc định 8080):
```
./mvnw spring-boot:run
```

Chạy frontend (cổng mặc định 5173, tự gọi API về `localhost:8080/api`):
```
cd frontend
npm run dev
```

Mở `http://localhost:5173`, vào thử ngay bằng nút dùng thử hoặc đăng ký tài khoản, sau đó có thể hỏi chatbot kiểu "gợi ý món dưới 50k gần tôi" hoặc "có món nào ít calo không".

API test bằng Postman: import collection trong thư mục `postman/`.

## Đóng góp

Fork repo, tạo nhánh mới từ `main` (`git checkout -b ten-nhanh`), commit thay đổi rồi tạo Pull Request. Mô tả rõ thay đổi làm gì trong PR để dễ review.

## Giấy phép

Đồ án phục vụ mục đích học tập, hiện chưa gắn giấy phép mã nguồn mở cụ thể.
