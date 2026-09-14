-- Seed data cho Food Recommendation System
-- Gop tu 4 file export pgAdmin: food, restaurant, nutrition, restaurant_food
-- Da bo dong ALTER ... OWNER TO (role rieng cua may Ray) va \restrict/\unrestrict
-- Chay 1 lan duy nhat tren database TRONG bang: psql -U <user> -d foodrecommendationdb -f seed.sql

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

-- ============ food ============

CREATE TABLE public.food (
    food_id integer NOT NULL,
    name character varying(150) NOT NULL,
    description text,
    cuisine_type character varying(100),
    price numeric(12,2),
    image_url character varying(500),
    CONSTRAINT ck_food_price CHECK (((price IS NULL) OR (price >= (0)::numeric)))
);

CREATE SEQUENCE public.food_food_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.food_food_id_seq OWNED BY public.food.food_id;

ALTER TABLE ONLY public.food ALTER COLUMN food_id SET DEFAULT nextval('public.food_food_id_seq'::regclass);

COPY public.food (food_id, name, description, cuisine_type, price, image_url) FROM stdin;
1	Phở bò	Phở bò truyền thống Việt Nam	Việt Nam	50000.00	pho-bo.jpg
2	Bún bò Huế	Bún bò đặc sản miền Trung	Việt Nam	55000.00	bun-bo-hue.jpg
3	Lẩu Thái Tomyum	Lẩu Thái chua cay	Thái Lan	180000.00	lau-thai-tomyum.jpg
4	Cơm tấm sườn	Cơm tấm với sườn nướng	Việt Nam	60000.00	com-tam.jpg
5	Sushi	Sushi Nhật Bản	Nhật Bản	150000.00	sushi.jpg
6	Phở Bò Tái	Phở bò truyền thống với thịt bò tái, nước dùng ninh xương 12 tiếng	Việt Nam	45000.00	https://example.com/images/pho-bo-tai.jpg
7	Bún Chả Hà Nội	Bún ăn kèm chả nướng than hoa và nước chấm chua ngọt	Việt Nam	40000.00	https://example.com/images/bun-cha.jpg
8	Cơm Tấm Sườn Bì Chả	Cơm tấm với sườn nướng, bì, chả trứng và nước mắm	Việt Nam	35000.00	https://example.com/images/com-tam.jpg
9	Bánh Mì Thịt Nướng	Bánh mì giòn kẹp thịt nướng, pate, rau thơm	Việt Nam	20000.00	https://example.com/images/banh-mi.jpg
10	Gỏi Cuốn Tôm Thịt	Gỏi cuốn tôm thịt tươi, ăn kèm nước mắm chấm	Việt Nam	30000.00	https://example.com/images/goi-cuon.jpg
11	Bún Bò Huế	Bún bò cay đặc trưng xứ Huế, nước dùng đậm đà	Việt Nam	42000.00	https://example.com/images/bun-bo-hue.jpg
12	Hủ Tiếu Nam Vang	Hủ tiếu nước trong với tôm, thịt bằm, gan heo	Việt Nam	38000.00	https://example.com/images/hu-tieu.jpg
13	Lẩu Thái Hải Sản	Lẩu chua cay kiểu Thái với hải sản tươi sống	Thái Lan	250000.00	https://example.com/images/lau-thai.jpg
14	Sushi Cá Hồi	Sushi cá hồi tươi cuộn cơm giấm Nhật Bản	Nhật Bản	120000.00	https://example.com/images/sushi-ca-hoi.jpg
15	Ramen Tonkotsu	Mì ramen nước lèo xương heo béo ngậy kiểu Nhật	Nhật Bản	95000.00	https://example.com/images/ramen.jpg
16	Kimbap Hàn Quốc	Cơm cuộn rong biển kiểu Hàn với rau củ và trứng	Hàn Quốc	55000.00	https://example.com/images/kimbap.jpg
17	Tokbokki Cay	Bánh gạo cay Hàn Quốc sốt gochujang	Hàn Quốc	48000.00	https://example.com/images/tokbokki.jpg
18	Pizza Hải Sản	Pizza đế mỏng phủ hải sản và phô mai mozzarella	Ý	180000.00	https://example.com/images/pizza-hai-san.jpg
19	Mì Ý Sốt Bò Bằm	Mì Ý sốt cà chua bò bằm kiểu Bolognese	Ý	85000.00	https://example.com/images/mi-y.jpg
20	Bò Bít Tết	Thăn bò Úc áp chảo ăn kèm khoai tây và rau củ	Âu	220000.00	https://example.com/images/bo-bit-tet.jpg
21	Gà Rán Giòn	Gà rán tẩm bột giòn rụm kiểu Mỹ	Mỹ	60000.00	https://example.com/images/ga-ran.jpg
22	Hamburger Phô Mai	Burger bò phô mai kẹp rau tươi và sốt đặc biệt	Mỹ	55000.00	https://example.com/images/hamburger.jpg
23	Dimsum Tổng Hợp	Set dimsum hấp đa dạng gồm há cảo, xíu mại, bánh bao	Trung Quốc	90000.00	https://example.com/images/dimsum.jpg
24	Vịt Quay Bắc Kinh	Vịt quay da giòn kiểu Bắc Kinh ăn kèm bánh tráng mỏng	Trung Quốc	150000.00	https://example.com/images/vit-quay.jpg
25	Chè Ba Màu	Chè đậu xanh, đậu đỏ, thạch nước cốt dừa (món chay)	Việt Nam	15000.00	https://example.com/images/che-ba-mau.jpg
26	Bánh Flan Caramel	Bánh flan trứng sữa mềm mịn phủ caramel (chay)	Việt Nam	12000.00	https://example.com/images/banh-flan.jpg
27	Trà Sữa Trân Châu	Trà sữa truyền thống với trân châu đen dẻo dai (chay)	Đài Loan	35000.00	https://example.com/images/tra-sua.jpg
28	Salad Cá Ngừ	Salad rau xanh với cá ngừ áp chảo, sốt mè rang	Âu	65000.00	https://example.com/images/salad-ca-ngu.jpg
29	Cháo Gà	Cháo gà nóng hổi thơm mùi hành lá và tiêu	Việt Nam	25000.00	https://example.com/images/chao-ga.jpg
30	Bánh Xèo Miền Tây	Bánh xèo giòn nhân tôm thịt giá đỗ, ăn kèm rau sống	Việt Nam	35000.00	https://example.com/images/banh-xeo.jpg
31	Phở Gà	Phở gà thanh ngọt, thịt gà xé mềm	Việt Nam	40000.00	\N
32	Cơm Tấm Sườn Nướng	Cơm tấm sườn nướng mật ong	Việt Nam	32000.00	\N
33	Bánh Mì Chả Cá	Bánh mì chả cá Nha Trang	Việt Nam	22000.00	\N
34	Hủ Tiếu Mì Sa Tế	Hủ tiếu mì cay sa tế	Việt Nam	35000.00	\N
35	Chả Giò Rế	Chả giò rế giòn rụm	Việt Nam	35000.00	\N
36	Mì Quảng Gà	Mì Quảng gà, đặc sản Đà Nẵng	Việt Nam	40000.00	\N
37	Cao Lầu Hội An	Cao lầu đặc sản phố cổ Hội An	Việt Nam	38000.00	\N
38	Bánh Canh Cua	Bánh canh cua đồng	Việt Nam	40000.00	\N
39	Pad Thai Tôm	Pad Thai xào tôm kiểu Thái	Thái Lan	65000.00	\N
40	Hải Sản Nướng Mọi	Set hải sản nướng tổng hợp	Hải sản	320000.00	\N
41	Tôm Hùm Nướng Phô Mai	Tôm hùm nướng phô mai béo ngậy	Hải sản	450000.00	\N
42	Gà Nướng Mật Ong	Gà nướng mật ong nguyên con	Việt Nam	180000.00	\N
43	Mì Ramen Tonkotsu	Ramen nước hầm xương heo đậm đà	Nhật Bản	95000.00	\N
44	Cơm Cà Ri Nhật	Cơm cà ri Nhật Bản kiểu truyền thống	Nhật Bản	75000.00	\N
45	Thịt Nướng Hàn Quốc	Thịt bò ướp sốt Hàn Quốc nướng (Bulgogi)	Hàn Quốc	150000.00	\N
46	Mì Ý Sốt Kem Nấm	Spaghetti sốt kem nấm	Ý	80000.00	\N
47	Pizza Margherita	Pizza phô mai cà chua basil truyền thống	Ý	150000.00	\N
48	Burger Bò Phô Mai	Burger bò phô mai kiểu Mỹ	Âu Mỹ	65000.00	\N
49	Salad Ức Gà Áp Chảo	Salad rau củ ức gà áp chảo, ít calo	Healthy	55000.00	\N
50	Salad Cá Hồi Sốt Mè Rang	Salad cá hồi áp chảo sốt mè rang	Healthy	75000.00	\N
51	Đậu Hũ Sốt Chua Ngọt Chay	Đậu hũ chiên sốt chua ngọt	Chay	35000.00	\N
52	Cơm Chay Thập Cẩm	Cơm chay đầy đủ dinh dưỡng	Chay	40000.00	\N
53	Bún Riêu Chay	Bún riêu nấm chay thanh đạm	Chay	35000.00	\N
54	Chè Khúc Bạch	Chè khúc bạch hạnh nhân	Tráng miệng	25000.00	\N
55	Bánh Flan Caramen	Bánh flan mềm mịn	Tráng miệng	15000.00	\N
56	Sinh Tố Bơ	Sinh tố bơ sáp béo ngậy	Đồ uống	30000.00	\N
57	Cà Phê Sữa Đá	Cà phê phin sữa đá truyền thống	Đồ uống	22000.00	\N
58	Bún Thang	Bún thang Hà Nội thanh đạm, nhiều nguyên liệu	Việt Nam	45000.00	\N
59	Bánh Cuốn	Bánh cuốn nóng nhân thịt mộc nhĩ	Việt Nam	30000.00	\N
60	Nem Nướng Nha Trang	Nem nướng cuốn bánh tráng, chấm sốt đặc biệt	Việt Nam	45000.00	\N
61	Bánh Bèo	Bánh bèo chén kiểu Huế, tôm cháy	Việt Nam	30000.00	\N
62	Bún Đậu Mắm Tôm	Bún đậu mắm tôm đầy đủ topping	Việt Nam	45000.00	\N
63	Cháo Lòng	Cháo lòng nóng hổi, đầy đủ nội tạng	Việt Nam	35000.00	\N
64	Xôi Gà	Xôi gà xé, hành phi thơm lừng	Việt Nam	30000.00	\N
65	Bánh Khọt	Bánh khọt Vũng Tàu giòn rụm nhân tôm	Việt Nam	40000.00	\N
66	Cơm Gà Hải Nam	Cơm gà luộc kiểu Hải Nam, nước chấm đặc trưng	Việt Nam	45000.00	\N
67	Vịt Nấu Chao	Vịt nấu chao béo ngậy ăn kèm bún	Việt Nam	65000.00	\N
68	Bò Kho	Bò kho sốt cà chua, ăn kèm bánh mì	Việt Nam	45000.00	\N
69	Cà Ri Gà	Cà ri gà nước cốt dừa đậm đà	Việt Nam	50000.00	\N
70	Bún Mắm	Bún mắm miền Tây đậm đà, đầy đủ hải sản	Việt Nam	45000.00	\N
71	Ốc Xào Me	Ốc xào me chua ngọt	Hải sản	60000.00	\N
72	Ghẹ Rang Muối	Ghẹ rang muối ớt thơm giòn	Hải sản	280000.00	\N
73	Tôm Nướng Muối Ớt	Tôm nướng muối ớt cay nồng	Hải sản	180000.00	\N
74	Cơm Chiên Dương Châu	Cơm chiên Dương Châu đầy đủ topping	Trung Hoa	55000.00	\N
75	Sủi Cảo	Sủi cảo tôm thịt nước dùng thanh	Trung Hoa	50000.00	\N
76	Mì Xào Giòn	Mì xào giòn thập cẩm sốt đặc	Trung Hoa	55000.00	\N
77	Dimsum Thập Cẩm	Set dimsum hấp đa dạng	Trung Hoa	90000.00	\N
78	Donburi Thịt Bò	Cơm thịt bò sốt Nhật kiểu donburi	Nhật Bản	85000.00	\N
79	Udon Tempura	Udon nước dùng thanh, tempura giòn	Nhật Bản	90000.00	\N
80	Bibimbap	Cơm trộn Hàn Quốc đầy đủ rau củ trứng	Hàn Quốc	75000.00	\N
81	Canh Kimchi Đậu Hũ	Ttukbaegi kimchi cay nồng ấm bụng	Hàn Quốc	70000.00	\N
82	Bánh Pancake Chuối	Pancake chuối phủ mật ong	Tráng miệng	30000.00	\N
83	Rau Câu Dừa	Rau câu nước dừa mát lạnh	Tráng miệng	20000.00	\N
84	Nước Ép Cam	Nước cam vắt tươi nguyên chất	Đồ uống	25000.00	\N
85	Matcha Latte	Trà xanh matcha sữa tươi	Đồ uống	40000.00	\N
\.

SELECT pg_catalog.setval('public.food_food_id_seq', 85, true);

ALTER TABLE ONLY public.food
    ADD CONSTRAINT food_pkey PRIMARY KEY (food_id);

CREATE INDEX ix_food_cuisine_type ON public.food USING btree (cuisine_type);


-- ============ restaurant ============

CREATE TABLE public.restaurant (
    restaurant_id integer NOT NULL,
    name character varying(200) NOT NULL,
    address character varying(500),
    latitude numeric(10,7),
    longitude numeric(10,7),
    rating numeric(2,1),
    opening_hours character varying(255),
    CONSTRAINT ck_restaurant_latitude CHECK (((latitude IS NULL) OR ((latitude >= ('-90'::integer)::numeric) AND (latitude <= (90)::numeric)))),
    CONSTRAINT ck_restaurant_longitude CHECK (((longitude IS NULL) OR ((longitude >= ('-180'::integer)::numeric) AND (longitude <= (180)::numeric)))),
    CONSTRAINT ck_restaurant_rating CHECK (((rating IS NULL) OR ((rating >= (0)::numeric) AND (rating <= (5)::numeric))))
);

CREATE SEQUENCE public.restaurant_restaurant_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.restaurant_restaurant_id_seq OWNED BY public.restaurant.restaurant_id;

ALTER TABLE ONLY public.restaurant ALTER COLUMN restaurant_id SET DEFAULT nextval('public.restaurant_restaurant_id_seq'::regclass);

COPY public.restaurant (restaurant_id, name, address, latitude, longitude, rating, opening_hours) FROM stdin;
1	Phở Hòa	260C Pasteur, Quận 3, Thành phố Hồ Chí Minh	10.7798000	106.6881000	4.3	06:00-22:00
2	Bún Bò Huế Đông Ba	110A Nguyễn Du, Quận 1, Thành phố Hồ Chí Minh	10.7745000	106.6992000	4.4	06:30-21:30
3	Thai House	123 Nguyễn Trãi, Quận 5, Thành phố Hồ Chí Minh	10.7567000	106.6712000	4.2	10:00-22:00
4	Cơm Tấm Sài Gòn	50 Võ Văn Tần, Quận 3, Thành phố Hồ Chí Minh	10.7753000	106.6878000	4.1	10:00-21:00
5	Phở Hòa Pasteur	260C Pasteur, Quận 3, TP.HCM	10.7802000	106.6953000	4.5	06:00 - 22:00
6	Bún Chả Hương Liên	24 Lê Văn Hưu, Hai Bà Trưng, Hà Nội	21.0167000	105.8544000	4.3	10:00 - 21:00
7	Cơm Tấm Ba Ghiền	84 Đặng Văn Ngữ, Phú Nhuận, TP.HCM	10.7969000	106.6763000	4.6	06:00 - 21:00
8	Bánh Mì Huỳnh Hoa	26 Lê Thị Riêng, Quận 1, TP.HCM	10.7715000	106.6917000	4.7	06:00 - 22:00
9	Nhà Hàng Lẩu Thái Tomyum	88 Nguyễn Huệ, Quận 1, TP.HCM	10.7738000	106.7030000	4.2	10:00 - 23:00
10	Sushi World	45 Nguyễn Trãi, Quận 5, TP.HCM	10.7550000	106.6789000	4.4	10:00 - 22:00
11	Kimchi House	12 Phan Xích Long, Phú Nhuận, TP.HCM	10.7975000	106.6844000	4.1	09:00 - 21:30
12	Pizza 4Ps	8/15 Lê Thánh Tôn, Quận 1, TP.HCM	10.7769000	106.7009000	4.8	10:00 - 22:30
13	Steak House Saigon	20 Đồng Khởi, Quận 1, TP.HCM	10.7765000	106.7038000	4.5	11:00 - 23:00
14	KFC Vincom	Vincom Center, Quận 1, TP.HCM	10.7796000	106.7013000	3.9	08:00 - 22:00
15	Dimsum Palace	168 Trần Hưng Đạo, Quận 5, TP.HCM	10.7546000	106.6698000	4.3	07:00 - 21:00
16	Chè Mười Sáu	16 Cao Thắng, Quận 3, TP.HCM	10.7787000	106.6832000	4.4	10:00 - 22:00
17	Bún Bò Huế Bình Dân	45 Trần Cao Vân, Quận 3, TP.HCM	10.7823000	106.6912000	4.2	06:00 - 21:00
18	Hủ Tiếu Nam Vang Kim Tân	88 Châu Văn Liêm, Quận 5, TP.HCM	10.7550000	106.6650000	4.3	06:00 - 22:00
19	Bún Chả Hà Nội 34	34 Nguyễn Đình Chiểu, Quận 3, TP.HCM	10.7841000	106.6935000	4.4	10:00 - 21:00
20	Bánh Xèo Ăn Là Ghiền	74A Sương Nguyệt Ánh, Quận 1, TP.HCM	10.7719000	106.6893000	4.3	09:00 - 21:30
21	Gỏi Cuốn Cô Ba	12 Mạc Thị Bưởi, Quận 1, TP.HCM	10.7758000	106.7031000	4.5	08:00 - 20:00
22	Lẩu Thái Tomyum House	200 Nguyễn Trãi, Quận 5, TP.HCM	10.7565000	106.6821000	4.4	11:00 - 23:00
23	Hải Sản Biển Đông	15 Nguyễn Văn Trỗi, Phú Nhuận, TP.HCM	10.7963000	106.6779000	4.6	10:00 - 22:00
24	Gà Nướng Bụi	120 Điện Biên Phủ, Bình Thạnh, TP.HCM	10.8012000	106.7107000	4.2	15:00 - 23:00
25	Sushi Tokyo Deli	Vincom Center, 72 Lê Thánh Tôn, Quận 1, TP.HCM	10.7797000	106.7020000	4.5	10:00 - 22:00
26	Kimchi House Korean BBQ	15 Tân Mỹ, Quận 7, TP.HCM	10.7327000	106.7218000	4.4	11:00 - 22:00
27	Mì Quảng Bà Mua	50 Trường Chinh, Tân Bình, TP.HCM	10.8014000	106.6478000	4.3	06:30 - 20:00
28	Cao Lầu Faifo	22 Lý Chính Thắng, Quận 3, TP.HCM	10.7896000	106.6857000	4.2	07:00 - 21:00
29	Chè Khúc Bạch Cô Hạnh	9 Hồ Biểu Chánh, Phú Nhuận, TP.HCM	10.7947000	106.6811000	4.6	09:00 - 22:00
30	The Coffee House Signature	86-88 Cao Thắng, Quận 3, TP.HCM	10.7770000	106.6821000	4.3	07:00 - 22:00
31	Vegan Garden	18 Ngô Thời Nhiệm, Quận 3, TP.HCM	10.7834000	106.6890000	4.5	08:00 - 21:00
32	Salad Box Healthy	5 Nguyễn Huệ, Quận 1, TP.HCM	10.7745000	106.7038000	4.4	07:00 - 20:00
33	Bún Thang Bà Ẩm	10 Hàng Bún, Quận 1, TP.HCM	10.7793000	106.6980000	4.3	06:30 - 20:00
34	Bánh Cuốn Thanh Trì	22 Nguyễn Thượng Hiền, Quận 3, TP.HCM	10.7822000	106.6858000	4.4	06:00 - 20:00
35	Nem Nướng Ngọc Sương	19 Trần Văn Đang, Quận 3, TP.HCM	10.7862000	106.6802000	4.5	09:00 - 21:00
36	Bún Đậu Homemade	8 Cao Thắng, Quận 3, TP.HCM	10.7772000	106.6813000	4.2	10:00 - 21:00
37	Cháo Lòng Bà Út	55 Trần Hưng Đạo, Quận 5, TP.HCM	10.7548000	106.6702000	4.1	05:30 - 12:00
38	Xôi Chè Bùi Thị Xuân	17 Bùi Thị Xuân, Quận 1, TP.HCM	10.7684000	106.6839000	4.4	06:00 - 22:00
39	Cơm Gà Hải Nam Đông Nguyên	89 Châu Văn Liêm, Quận 5, TP.HCM	10.7539000	106.6635000	4.5	09:00 - 21:00
40	Ốc Đào	40 Vĩnh Khánh, Quận 4, TP.HCM	10.7583000	106.7004000	4.3	16:00 - 23:30
41	Sủi Cảo Chú Mập	90 Hà Tôn Quyền, Quận 11, TP.HCM	10.7580000	106.6485000	4.4	10:00 - 21:00
42	Sushi Kei	22 Nguyễn Văn Hưởng, Quận 2, TP.HCM	10.8025000	106.7385000	4.6	10:30 - 22:00
43	Bibimbap House	10 Tân Mỹ, Quận 7, TP.HCM	10.7310000	106.7205000	4.4	11:00 - 21:30
44	Juice Bar Fresh	3 Đồng Khởi, Quận 1, TP.HCM	10.7778000	106.7030000	4.3	07:00 - 21:00
45	Quán Ăn Cô Tư	120 Lý Chính Thắng, Quận 3, TP.HCM	10.7877000	106.6863000	4.3	06:00 - 21:00
46	Quán Ăn Gia Đình	45 Sư Vạn Hạnh, Quận 10, TP.HCM	10.7692000	106.6674000	4.2	06:00 - 20:30
47	Food Court Bến Thành	Chợ Bến Thành, Quận 1, TP.HCM	10.7724000	106.6981000	4.1	07:00 - 19:00
\.

SELECT pg_catalog.setval('public.restaurant_restaurant_id_seq', 47, true);

ALTER TABLE ONLY public.restaurant
    ADD CONSTRAINT restaurant_pkey PRIMARY KEY (restaurant_id);

CREATE INDEX ix_restaurant_location ON public.restaurant USING btree (latitude, longitude);


-- ============ nutrition (FK -> food) ============

CREATE TABLE public.nutrition (
    nutrition_id integer NOT NULL,
    food_id integer NOT NULL,
    calories numeric(10,2),
    protein numeric(10,2),
    fat numeric(10,2),
    carbohydrates numeric(10,2),
    CONSTRAINT ck_nutrition_calories CHECK (((calories IS NULL) OR (calories >= (0)::numeric))),
    CONSTRAINT ck_nutrition_carbohydrates CHECK (((carbohydrates IS NULL) OR (carbohydrates >= (0)::numeric))),
    CONSTRAINT ck_nutrition_fat CHECK (((fat IS NULL) OR (fat >= (0)::numeric))),
    CONSTRAINT ck_nutrition_protein CHECK (((protein IS NULL) OR (protein >= (0)::numeric)))
);

CREATE SEQUENCE public.nutrition_nutrition_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.nutrition_nutrition_id_seq OWNED BY public.nutrition.nutrition_id;

ALTER TABLE ONLY public.nutrition ALTER COLUMN nutrition_id SET DEFAULT nextval('public.nutrition_nutrition_id_seq'::regclass);

COPY public.nutrition (nutrition_id, food_id, calories, protein, fat, carbohydrates) FROM stdin;
1	1	450.00	25.00	12.00	60.00
2	2	550.00	30.00	18.00	65.00
3	3	700.00	35.00	30.00	70.00
4	4	650.00	35.00	22.00	75.00
5	5	500.00	25.00	15.00	55.00
6	6	450.00	25.00	12.00	55.00
7	7	520.00	28.00	22.00	48.00
8	8	650.00	30.00	25.00	70.00
9	9	400.00	18.00	15.00	45.00
10	10	180.00	12.00	4.00	22.00
11	11	480.00	26.00	18.00	50.00
12	12	420.00	20.00	10.00	55.00
13	13	600.00	35.00	20.00	40.00
14	14	350.00	22.00	10.00	45.00
15	15	550.00	24.00	22.00	60.00
16	16	380.00	14.00	10.00	55.00
17	17	420.00	10.00	8.00	75.00
18	18	800.00	32.00	30.00	90.00
19	19	620.00	26.00	18.00	75.00
20	20	550.00	45.00	30.00	10.00
21	21	500.00	28.00	28.00	30.00
22	22	550.00	25.00	30.00	40.00
23	23	480.00	20.00	18.00	55.00
24	24	620.00	35.00	35.00	25.00
25	25	250.00	4.00	5.00	48.00
26	26	220.00	6.00	8.00	32.00
27	27	320.00	3.00	8.00	60.00
28	28	280.00	30.00	10.00	12.00
29	29	320.00	20.00	8.00	40.00
30	30	480.00	18.00	22.00	50.00
31	31	380.00	22.00	7.00	55.00
32	32	600.00	28.00	20.00	68.00
33	33	380.00	16.00	12.00	48.00
34	34	450.00	20.00	15.00	55.00
35	35	350.00	10.00	20.00	30.00
36	36	460.00	24.00	12.00	58.00
37	37	440.00	20.00	10.00	60.00
38	38	400.00	18.00	12.00	50.00
39	39	480.00	22.00	15.00	55.00
40	40	600.00	45.00	25.00	20.00
41	41	700.00	40.00	40.00	15.00
42	42	550.00	40.00	25.00	20.00
43	43	550.00	25.00	20.00	60.00
44	44	500.00	18.00	15.00	65.00
45	45	480.00	35.00	25.00	10.00
46	46	600.00	18.00	25.00	65.00
47	47	650.00	22.00	20.00	78.00
48	48	600.00	30.00	30.00	45.00
49	49	320.00	30.00	10.00	20.00
50	50	380.00	28.00	18.00	15.00
51	51	280.00	12.00	12.00	30.00
52	52	450.00	15.00	10.00	70.00
53	53	350.00	12.00	8.00	50.00
54	54	220.00	4.00	6.00	35.00
55	55	180.00	4.00	6.00	28.00
56	56	280.00	4.00	14.00	35.00
57	57	150.00	3.00	6.00	20.00
58	58	420.00	22.00	10.00	55.00
59	59	350.00	14.00	8.00	55.00
60	60	480.00	22.00	20.00	45.00
61	61	320.00	10.00	8.00	50.00
62	62	550.00	20.00	28.00	50.00
63	63	400.00	22.00	15.00	45.00
64	64	480.00	20.00	15.00	65.00
65	65	420.00	14.00	20.00	45.00
66	66	550.00	30.00	18.00	60.00
67	67	580.00	28.00	30.00	40.00
68	68	500.00	26.00	22.00	40.00
69	69	520.00	24.00	25.00	45.00
70	70	460.00	24.00	14.00	55.00
71	71	300.00	18.00	10.00	30.00
72	72	450.00	35.00	20.00	15.00
73	73	380.00	32.00	15.00	10.00
74	74	600.00	20.00	20.00	75.00
75	75	420.00	22.00	12.00	50.00
76	76	580.00	20.00	22.00	65.00
77	77	500.00	24.00	18.00	55.00
78	78	620.00	28.00	22.00	70.00
79	79	560.00	20.00	18.00	75.00
80	80	550.00	22.00	18.00	70.00
81	81	380.00	20.00	15.00	30.00
82	82	350.00	6.00	12.00	55.00
83	83	150.00	2.00	4.00	28.00
84	84	110.00	2.00	0.00	25.00
85	85	220.00	6.00	8.00	32.00
\.

SELECT pg_catalog.setval('public.nutrition_nutrition_id_seq', 85, true);

ALTER TABLE ONLY public.nutrition
    ADD CONSTRAINT nutrition_pkey PRIMARY KEY (nutrition_id);

ALTER TABLE ONLY public.nutrition
    ADD CONSTRAINT uq_nutrition_food UNIQUE (food_id);

ALTER TABLE ONLY public.nutrition
    ADD CONSTRAINT fk_nutrition_food FOREIGN KEY (food_id) REFERENCES public.food(food_id) ON UPDATE CASCADE ON DELETE CASCADE;


-- ============ restaurant_food (FK -> food, restaurant) ============

CREATE TABLE public.restaurant_food (
    restaurant_id integer NOT NULL,
    food_id integer NOT NULL,
    price numeric(12,2),
    is_available boolean DEFAULT true NOT NULL,
    CONSTRAINT ck_restaurant_food_price CHECK (((price IS NULL) OR (price >= (0)::numeric)))
);

COPY public.restaurant_food (restaurant_id, food_id, price, is_available) FROM stdin;
1	1	55000.00	t
2	2	60000.00	t
3	1	60000.00	t
3	3	199000.00	t
4	4	65000.00	t
5	1	50000.00	t
5	6	45000.00	t
5	11	42000.00	t
5	29	25000.00	t
5	31	40000.00	t
6	7	40000.00	t
6	10	30000.00	t
7	8	35000.00	t
7	30	35000.00	t
7	32	32000.00	t
8	9	20000.00	t
8	12	38000.00	f
8	33	22000.00	t
9	13	250000.00	t
9	16	55000.00	t
10	14	120000.00	t
10	15	95000.00	t
11	16	55000.00	t
11	17	48000.00	t
12	18	180000.00	t
12	19	85000.00	t
12	46	80000.00	t
12	47	150000.00	t
13	20	220000.00	t
13	28	65000.00	t
14	21	60000.00	t
14	22	55000.00	t
15	23	90000.00	t
15	24	150000.00	t
16	25	15000.00	t
16	26	12000.00	t
16	27	35000.00	t
17	2	42000.00	t
17	11	42000.00	t
18	12	38000.00	t
18	34	35000.00	t
19	7	45000.00	t
19	35	35000.00	t
20	10	30000.00	t
20	30	40000.00	t
21	10	30000.00	t
21	35	35000.00	t
21	53	35000.00	t
22	3	250000.00	t
22	39	65000.00	t
23	40	320000.00	t
23	41	450000.00	t
24	24	220000.00	t
24	42	180000.00	t
25	14	120000.00	t
25	43	95000.00	t
25	44	75000.00	t
26	16	45000.00	t
26	17	40000.00	t
26	45	150000.00	t
27	36	40000.00	t
27	37	38000.00	t
27	38	40000.00	t
28	36	40000.00	t
28	37	38000.00	t
29	25	20000.00	t
29	54	25000.00	t
29	55	15000.00	t
30	27	35000.00	t
30	56	30000.00	t
30	57	22000.00	t
31	51	35000.00	t
31	52	40000.00	t
31	53	35000.00	t
32	48	65000.00	t
32	49	55000.00	t
32	50	75000.00	t
33	35	35000.00	t
33	58	45000.00	t
34	35	35000.00	t
34	59	30000.00	t
35	10	30000.00	t
35	60	45000.00	t
36	35	35000.00	t
36	62	45000.00	t
37	61	30000.00	t
37	63	35000.00	t
38	25	15000.00	t
38	54	25000.00	t
38	64	30000.00	t
39	24	150000.00	t
39	66	45000.00	t
40	71	60000.00	t
40	72	280000.00	t
40	73	180000.00	t
41	74	55000.00	t
41	75	50000.00	t
41	76	55000.00	t
41	77	90000.00	t
42	14	120000.00	t
42	43	95000.00	t
42	78	85000.00	t
42	79	90000.00	t
43	17	48000.00	t
43	80	75000.00	t
43	81	70000.00	t
44	27	35000.00	t
44	56	30000.00	t
44	84	25000.00	t
44	85	40000.00	t
45	1	48000.00	t
45	12	38000.00	t
45	32	32000.00	t
45	68	45000.00	t
46	7	40000.00	t
46	9	20000.00	t
46	31	38000.00	t
46	63	35000.00	t
46	69	50000.00	t
47	6	47000.00	t
47	8	35000.00	t
47	10	30000.00	t
47	30	35000.00	t
47	34	35000.00	t
47	70	45000.00	t
\.

ALTER TABLE ONLY public.restaurant_food
    ADD CONSTRAINT restaurant_food_pkey PRIMARY KEY (restaurant_id, food_id);

ALTER TABLE ONLY public.restaurant_food
    ADD CONSTRAINT fk_restaurant_food_food FOREIGN KEY (food_id) REFERENCES public.food(food_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY public.restaurant_food
    ADD CONSTRAINT fk_restaurant_food_restaurant FOREIGN KEY (restaurant_id) REFERENCES public.restaurant(restaurant_id) ON UPDATE CASCADE ON DELETE CASCADE;

