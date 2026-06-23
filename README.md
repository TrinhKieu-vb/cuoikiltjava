# KT-Shop  -  Hệ thống quản lý cửa hàng đồ uống & tráng miệng

## Giới thiệu
KT-Shop là hệ thống quản lý đồ uống và tráng miệng được xây dựng bằng ngôn ngữ Java nhằm hỗ trợ việc quản lý hoạt động kinh doanh tại cửa hàng. Hệ thống cho phép quản lý sản phẩm, danh mục sản phẩm, khách hàng, hóa đơn và người dùng, đồng thời hỗ trợ thống kê dữ liệu phục vụ công tác quản lý. Dự án được thực hiện nhằm vận dụng các kiến thức đã học về lập trình Java, lập trình giao diện JavaFX, kết nối cơ sở dữ liệu MySQL và mô hình Client-Server vào một bài toán thực tế.

## Mục tiêu dự án
Mục tiêu của dự án là xây dựng một hệ thống quản lý bán hàng đơn giản nhưng đầy đủ các chức năng cơ bản cần thiết cho một cửa hàng đồ uống và tráng miệng. Thông qua dự án, nhóm tác giả mong muốn áp dụng các kiến thức về lập trình hướng đối tượng, thiết kế giao diện người dùng, xử lý cơ sở dữ liệu và truyền thông mạng trong Java. Ngoài ra, dự án còn giúp nâng cao kỹ năng phân tích yêu cầu, thiết kế hệ thống và tổ chức mã nguồn theo mô hình nhiều lớp.

## Đối tượng sử dụng
Hệ thống được thiết kế cho hai nhóm người dùng chính:
- Quản trị viên (Admin): Có quyền quản lý toàn bộ hệ thống, bao gồm quản lý người dùng, sản phẩm, danh mục, khách hàng, hóa đơn và theo dõi thống kê.
- Nhân viên (Staff): Có quyền thực hiện các nghiệp vụ bán hàng, quản lý khách hàng và tra cứu thông tin hóa đơn.

## Chức năng nổi bật
Hệ thống cung cấp các chức năng chính sau:
- Đăng nhập và phân quyền người dùng.
- Quản lý sản phẩm (thêm, sửa, xóa, tìm kiếm).
- Quản lý danh mục sản phẩm.
- Quản lý khách hàng.
- Quản lý hóa đơn bán hàng.
- Xem chi tiết hóa đơn.
- Thống kê số lượng sản phẩm, khách hàng và doanh thu.
- Import và Export dữ liệu.
- Ghi nhật ký hoạt động của hệ thống.
  
## Công nghệ sử dụng
Dự án được phát triển bằng các công nghệ và công cụ sau:
- Java 21
- JavaFX
- MySQL
- JDBC
- Maven
- Socket Programming
- Gson
- Logback
- SLF4J

## Kiến trúc hệ thống
Hệ thống được xây dựng theo mô hình Client-Server.
Client (JavaFX)
       |
       v
Client Service
       |
       v
Socket Connection
       |
       v
Server
       |
       v
DAO Layer
       |
       v
MySQL Database

Quy trình xử lý dữ liệu được thực hiện như sau:
1. Người dùng thao tác trên giao diện Client.
2. Client gửi yêu cầu đến Server thông qua Socket.
3. Server tiếp nhận và xử lý yêu cầu.
4. DAO thực hiện truy vấn cơ sở dữ liệu MySQL.
5. Kết quả được trả về Client để hiển thị cho người dùng.

## Cơ sở dữ liệu
Hệ thống sử dụng cơ sở dữ liệu MySQL với các bảng chính:
users
products
categories
customers
orders
order_details
Các bảng được liên kết với nhau nhằm đảm bảo việc quản lý dữ liệu sản phẩm, khách hàng và hóa đơn được thực hiện một cách nhất quán và hiệu quả.

## Hướng dẫn cài đặt

### Bước 1: Clone dự án
git clone https://github.com/your-username/cuoikijava.git
### Bước 2: Tạo cơ sở dữ liệu
Tạo cơ sở dữ liệu mới trong MySQL và import file SQL được cung cấp trong dự án.
CREATE DATABASE beverage_management;
### Bước 3: Cấu hình kết nối cơ sở dữ liệu
Mở file DatabaseUtil.java và chỉnh sửa các thông số kết nối phù hợp với máy tính của người dùng.
private static final String URL = "jdbc:mysql://localhost:3306/beverage_management";
private static final String USER = "root";
private static final String PASSWORD = ":))) hehehe hí hí hí kkkkkk";
### Bước 4: Khởi động Server
Chạy lớp Server.java để khởi động máy chủ.
### Bước 5: Khởi động Client
Chạy lớp App.java để mở giao diện chương trình.

## Kết luận
Dự án Hệ thống quản lí cửa hàng đồ uống và tráng miệng là một ứng dụng quản lý bán hàng được xây dựng bằng Java theo mô hình Client-Server. Hệ thống đáp ứng các yêu cầu cơ bản của một cửa hàng đồ uống và tráng miệng, đồng thời là cơ hội để vận dụng các kiến thức về lập trình Java, cơ sở dữ liệu và phát triển phần mềm trong thực tế.

## Tác giả
Ngô Thị Kiều Trinh - 24AI062
Dự án được thực hiện phục vụ học tập và nghiên cứu trong môn Lập trình Java.
