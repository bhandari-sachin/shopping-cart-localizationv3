-- Create Database
CREATE DATABASE IF NOT EXISTS shopping_cart_localization
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shopping_cart_localization;

-- Cart Records Table
CREATE TABLE IF NOT EXISTS cart_records (
                                            id INT AUTO_INCREMENT PRIMARY KEY,
                                            total_items INT NOT NULL,
                                            total_cost DOUBLE NOT NULL,
                                            language VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Cart Items Table
CREATE TABLE IF NOT EXISTS cart_items (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          cart_record_id INT,
                                          item_number INT NOT NULL,
                                          price DOUBLE NOT NULL,
                                          quantity INT NOT NULL,
                                          subtotal DOUBLE NOT NULL,
                                          FOREIGN KEY (cart_record_id) REFERENCES cart_records(id) ON DELETE CASCADE
    );

-- Localization Strings Table
CREATE TABLE IF NOT EXISTS localization_strings (
                                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                                    `key` VARCHAR(100) NOT NULL,
    value VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL
    );

-- Insert English Strings
INSERT INTO localization_strings (`key`, value, language) VALUES
                                                              ('select.language', 'Select Language:', 'en'),
                                                              ('prompt.num.items', 'Number of items:', 'en'),
                                                              ('btn.generate.items', 'Generate Items', 'en'),
                                                              ('btn.calculate.total', 'Calculate Total', 'en'),
                                                              ('total.cost', 'Total Cost:', 'en'),
                                                              ('prompt.price', 'Price', 'en'),
                                                              ('prompt.quantity', 'Quantity', 'en'),
                                                              ('item.prompt', 'Item', 'en'),
                                                              ('error.invalid.number', 'Invalid number format', 'en'),
                                                              ('error.positive.number', 'Please enter a positive number', 'en');

-- Insert Finnish Strings
INSERT INTO localization_strings (`key`, value, language) VALUES
                                                              ('select.language', 'Valitse kieli:', 'fi'),
                                                              ('prompt.num.items', 'Tuotteiden määrä:', 'fi'),
                                                              ('btn.generate.items', 'Luo tuotteet', 'fi'),
                                                              ('btn.calculate.total', 'Laske yhteensä', 'fi'),
                                                              ('total.cost', 'Kokonaishinta:', 'fi'),
                                                              ('prompt.price', 'Hinta', 'fi'),
                                                              ('prompt.quantity', 'Määrä', 'fi'),
                                                              ('item.prompt', 'Tuote', 'fi'),
                                                              ('error.invalid.number', 'Virheellinen numeromuoto', 'fi'),
                                                              ('error.positive.number', 'Anna positiivinen luku', 'fi');

-- Insert Swedish Strings
INSERT INTO localization_strings (`key`, value, language) VALUES
                                                              ('select.language', 'Välj språk:', 'sv'),
                                                              ('prompt.num.items', 'Antal artiklar:', 'sv'),
                                                              ('btn.generate.items', 'Generera artiklar', 'sv'),
                                                              ('btn.calculate.total', 'Beräkna totalt', 'sv'),
                                                              ('total.cost', 'Totalkostnad:', 'sv'),
                                                              ('prompt.price', 'Pris', 'sv'),
                                                              ('prompt.quantity', 'Antal', 'sv'),
                                                              ('item.prompt', 'Artikel', 'sv'),
                                                              ('error.invalid.number', 'Ogiltigt nummerformat', 'sv'),
                                                              ('error.positive.number', 'Ange ett positivt tal', 'sv');

-- Insert Japanese Strings
INSERT INTO localization_strings (`key`, value, language) VALUES
                                                              ('select.language', '言語を選択:', 'ja'),
                                                              ('prompt.num.items', 'アイテム数:', 'ja'),
                                                              ('btn.generate.items', 'アイテムを生成', 'ja'),
                                                              ('btn.calculate.total', '合計を計算', 'ja'),
                                                              ('total.cost', '合計金額:', 'ja'),
                                                              ('prompt.price', '価格', 'ja'),
                                                              ('prompt.quantity', '数量', 'ja'),
                                                              ('item.prompt', 'アイテム', 'ja'),
                                                              ('error.invalid.number', '無効な数値形式', 'ja'),
                                                              ('error.positive.number', '正の数を入力してください', 'ja');

-- Insert Arabic Strings
INSERT INTO localization_strings (`key`, value, language) VALUES
                                                              ('select.language', 'اختر اللغة:', 'ar'),
                                                              ('prompt.num.items', 'عدد العناصر:', 'ar'),
                                                              ('btn.generate.items', 'إنشاء عناصر', 'ar'),
                                                              ('btn.calculate.total', 'حساب المجموع', 'ar'),
                                                              ('total.cost', 'التكلفة الإجمالية:', 'ar'),
                                                              ('prompt.price', 'السعر', 'ar'),
                                                              ('prompt.quantity', 'الكمية', 'ar'),
                                                              ('item.prompt', 'عنصر', 'ar'),
                                                              ('error.invalid.number', 'تنسيق رقم غير صالح', 'ar'),
                                                              ('error.positive.number', 'يرجى إدخال رقم موجب', 'ar');