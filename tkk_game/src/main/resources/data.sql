INSERT INTO koma VALUES (0,'王将', NULL,  -1);
INSERT INTO koma VALUES (1,'歩兵', NULL,  8);
INSERT INTO koma VALUES (2,'香車', NULL,  9);
INSERT INTO koma VALUES (3,'桂馬', NULL,  10);
INSERT INTO koma VALUES (4,'銀将', NULL,  11);
INSERT INTO koma VALUES (5,'金将', NULL,  -1);
INSERT INTO koma VALUES (6,'角行', NULL,  12);
INSERT INTO koma VALUES (7,'飛車', NULL,  13);
INSERT INTO koma VALUES (8,'と金', NULL,  -1);
INSERT INTO koma VALUES (9,'成香', NULL,  -1);
INSERT INTO koma VALUES (10,'成桂', NULL, -1);
INSERT INTO koma VALUES (11,'成銀', NULL, -1);
INSERT INTO koma VALUES (12,'馬', NULL, -1);
INSERT INTO koma VALUES (13,'龍', NULL, -1);

INSERT INTO koma VALUES (14,'忍び', 'STEALTH', -1);


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
INSERT INTO KomaRule (koma_id, rule) VALUES (8, 'UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (8, 'DOWN');
INSERT INTO KomaRule (koma_id, rule) VALUES (8, 'LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (8, 'RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (8, 'UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (8, 'UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (9, 'UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (9, 'DOWN');
INSERT INTO KomaRule (koma_id, rule) VALUES (9, 'LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (9, 'RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (9, 'UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (9, 'UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (10, 'UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (10, 'DOWN');
INSERT INTO KomaRule (koma_id, rule) VALUES (10, 'LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (10, 'RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (10, 'UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (10, 'UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (11, 'UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (11, 'DOWN');
INSERT INTO KomaRule (koma_id, rule) VALUES (11, 'LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (11, 'RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (11, 'UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (11, 'UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (12, 'UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (12, 'DOWN');
INSERT INTO KomaRule (koma_id, rule) VALUES (12, 'LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (12, 'RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (12, 'LINE_UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (12, 'LINE_UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (12, 'LINE_DOWN_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (12, 'LINE_DOWN_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (13, 'UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (13, 'UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (13, 'DOWN_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (13, 'DOWN_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (13, 'LINE_UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (13, 'LINE_DOWN');
INSERT INTO KomaRule (koma_id, rule) VALUES (13, 'LINE_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (13, 'LINE_RIGHT');

INSERT INTO KomaRule (koma_id, rule) VALUES (14, 'UP');
INSERT INTO KomaRule (koma_id, rule) VALUES (14, 'UP_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (14, 'UP_RIGHT');
INSERT INTO KomaRule (koma_id, rule) VALUES (14, 'DOWN_LEFT');
INSERT INTO KomaRule (koma_id, rule) VALUES (14, 'DOWN_RIGHT');

-- komaテーブルのAUTO_INCREMENTシーケンスを次のIDに設定
ALTER TABLE koma ALTER COLUMN id RESTART WITH 15;

INSERT INTO Deck (name, sfen, cost) VALUES ('共通デッキ(王のみ)', '5/2[0]2', 8);
INSERT INTO Deck (name, sfen, cost) VALUES ('共通デッキ(デバッグ用)', '5/[7][3][0][1][14]', 35);
INSERT INTO Deck (name, sfen, cost) VALUES ('user1のデッキ', '5/2[0]2', 8);
INSERT INTO Deck (name, sfen, cost) VALUES ('user2のデッキ', '5/2[0]2', 8);
INSERT INTO Deck (name, sfen, cost) VALUES ('user3のデッキ', '5/2[0]2', 8);
INSERT INTO Deck (name, sfen, cost) VALUES ('user4のデッキ', '5/2[0]2', 8);

INSERT INTO Player (username, selected_deck_id) VALUES ('user1', 3);
INSERT INTO Player (username, selected_deck_id) VALUES ('user2', 4);
INSERT INTO Player (username, selected_deck_id) VALUES ('user3', 5);
INSERT INTO Player (username, selected_deck_id) VALUES ('user4', 6);

-- プレイヤーが使えるデッキを登録
-- 共通デッキは全プレイヤーが使用可能（所有者なし）
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (1, 1, FALSE);  -- user1 -> 共通デッキ(王のみ)
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (1, 2, FALSE);  -- user1 -> 共通デッキ(デバッグ用)
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (2, 1, FALSE);  -- user2 -> 共通デッキ(王のみ)
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (2, 2, FALSE);  -- user2 -> 共通デッキ(デバッグ用)
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (3, 1, FALSE);  -- user3 -> 共通デッキ(王のみ)
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (3, 2, FALSE);  -- user3 -> 共通デッキ(デバッグ用)
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (4, 1, FALSE);  -- user4 -> 共通デッキ(王のみ)
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (4, 2, FALSE);  -- user4 -> 共通デッキ(デバッグ用)

-- 各プレイヤーの個人デッキを登録（所有者として）
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (1, 3, TRUE);  -- user1 -> user1のデッキ（所有者）
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (2, 4, TRUE);  -- user2 -> user2のデッキ（所有者）
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (3, 5, TRUE);  -- user3 -> user3のデッキ（所有者）
INSERT INTO PlayerDeck (player_id, deck_id, is_owner) VALUES (4, 6, TRUE);  -- user4 -> user4のデッキ（所有者）
