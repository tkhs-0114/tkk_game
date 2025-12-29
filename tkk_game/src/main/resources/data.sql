INSERT INTO koma VALUES (0,'王将', -1);
INSERT INTO koma VALUES (1,'歩兵', 5);
INSERT INTO koma VALUES (2,'香車', 5);
INSERT INTO koma VALUES (3,'桂馬', 5);
INSERT INTO koma VALUES (4,'銀将', 5);
INSERT INTO koma VALUES (5,'金将', 5);
INSERT INTO koma VALUES (6,'角行', 5);
INSERT INTO koma VALUES (7,'飛車', 5);


INSERT INTO KomaRule (koma_id, rule) VALUES (0, 'UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (0, 'DOWN');
INSERT INTO KomaRule (koma_id, rule) VALUES (0, 'LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (0, 'RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (0, 'UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (0, 'UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (0, 'DOWN_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (0, 'DOWN_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (1, 'UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (2, 'LINE_UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (3, 'JUMP_UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (3, 'JUMP_UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (4, 'UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (4, 'UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (4, 'UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (4, 'DOWN_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (4, 'DOWN_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (5, 'UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (5, 'DOWN');
INSERT INTO KomaRule (koma_id, rule) VALUES (5, 'LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (5, 'RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (5, 'UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (5, 'UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (6, 'LINE_UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (6, 'LINE_UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (6, 'LINE_DOWN_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (6, 'LINE_DOWN_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (7, 'LINE_UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (7, 'LINE_DOWN');
INSERT INTO KomaRule (koma_id, rule) VALUES (7, 'LINE_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (7, 'LINE_RIGHT');

INSERT INTO Deck (name, sfen) VALUES ('sample', '5/2[0]2');

INSERT INTO Player (username) VALUES ('user1');
INSERT INTO Player (username) VALUES ('user2');
INSERT INTO Player (username) VALUES ('user3');
INSERT INTO Player (username) VALUES ('user4');
