-- V3__seed_cities.sql
-- Reference data: Bulgarian cities (always seeded, not dev-only)

INSERT INTO cities (slug, name_bg, name_en, latitude, longitude)
VALUES
    ('sofia',           'София',          'Sofia',          42.697500, 23.324200),
    ('plovdiv',         'Пловдив',        'Plovdiv',        42.135400, 24.745300),
    ('varna',           'Варна',          'Varna',          43.214100, 27.914700),
    ('burgas',          'Бургас',         'Burgas',         42.504800, 27.462600),
    ('ruse',            'Русе',           'Ruse',           43.856400, 25.970400),
    ('stara-zagora',    'Стара Загора',   'Stara Zagora',   42.425800, 25.634500),
    ('pleven',          'Плевен',         'Pleven',         43.417000, 24.616900),
    ('sliven',          'Сливен',         'Sliven',         42.682400, 26.322900),
    ('dobrich',         'Добрич',         'Dobrich',        43.572600, 27.827200),
    ('shumen',          'Шумен',          'Shumen',         43.270800, 26.936400),
    ('pernik',          'Перник',         'Pernik',         42.606000, 23.035600),
    ('haskovo',         'Хасково',        'Haskovo',        41.934400, 25.555600),
    ('yambol',          'Ямбол',          'Yambol',         42.484100, 26.503500),
    ('pazardzhik',      'Пазарджик',      'Pazardzhik',     42.192800, 24.334500),
    ('blagoevgrad',     'Благоевград',    'Blagoevgrad',    42.020800, 23.096800),
    ('veliko-tarnovo',  'Велико Търново', 'Veliko Tarnovo', 43.075700, 25.617200),
    ('vratsa',          'Враца',          'Vratsa',         43.210200, 23.562800),
    ('gabrovo',         'Габрово',        'Gabrovo',        42.874200, 25.318300),
    ('vidin',           'Видин',          'Vidin',          43.990200, 22.882300),
    ('kardzhali',       'Кърджали',       'Kardzhali',      41.633900, 25.375200),
    ('kyustendil',      'Кюстендил',      'Kyustendil',     42.283300, 22.690000),
    ('lovech',          'Ловеч',          'Lovech',         43.133800, 24.714500),
    ('montana',         'Монтана',        'Montana',        43.412700, 23.225700),
    ('razgrad',         'Разград',        'Razgrad',        43.533300, 26.516700),
    ('silistra',        'Силистра',       'Silistra',       44.117200, 27.261100),
    ('smolyan',         'Смолян',         'Smolyan',        41.577200, 24.701400),
    ('targovishte',     'Търговище',      'Targovishte',    43.250000, 26.570000),
    ('bansko',          'Банско',         'Bansko',         41.837800, 23.487800),
    ('sandanski',       'Сандански',      'Sandanski',      41.563600, 23.276400)
ON CONFLICT (slug) DO NOTHING;
