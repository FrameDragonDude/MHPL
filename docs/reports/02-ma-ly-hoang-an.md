# Bao cao module: Quan ly Thi sinh va Hien thi du lieu lon

## Pham vi cong viec
- Quan ly thong tin thi sinh: xem danh sach, sua thong tin co ban.
- Tim kiem thi sinh theo CCCD va ho ten.
- Phan trang 20 dong/trang de toi uu truy van va toc do hien thi.

## Thiet ke ky thuat
- Tang DTO: `CandidateDTO` de dong goi du lieu thi sinh.
- Tang DAO: `CandidateDAO` truy van MySQL voi `LIMIT` va `OFFSET`.
- Tang BUS: `CandidateService` xu ly quy tac phan trang (`PAGE_SIZE = 20`) va validate co ban.
- Tang GUI: `CandidatePanel` dung `JTable` + bo loc + dieu huong trang.

## Tim kiem va phan trang
- Bo loc CCCD: dieu kien `cccd LIKE ?`.
- Bo loc ho ten: ghep cot `ho` + `ten` va dung `LIKE`.
- Truy van danh sach:
	- `ORDER BY idthisinh ASC`
	- `LIMIT 20 OFFSET (page-1)*20`
- Truy van dem tong ban ghi dung de tinh tong so trang.

## Toi uu hien thi Swing
- Chi tai du lieu cua trang hien tai, tranh nap toan bo bang lon vao RAM.
- Mo hinh bang `DefaultTableModel` don gian, de thay the bang model tuy chinh khi can.
- Tach logic truy van ra DAO de GUI khong bi nghen boi xu ly SQL.

## Huong mo rong
- Them debounce cho o tim kiem de giam so lan truy van.
- Them cache metadata (tong so dong) trong khoang thoi gian ngan.
- Bo sung sua theo form popup va validate nang cao (email, so dien thoai, ngay sinh).
