/**
 * 駒の文字数に応じてフォントサイズを動的に調整する関数
 * @param {HTMLElement} element - サイズを調整する要素
 */
function adjustKomaFontSize(element) {
  if (!element) return;
  const textContent = element.textContent.trim();
  const charCount = textContent.length;
  
  // 文字数に応じてフォントサイズを決定
  let fontSize;
  if (charCount <= 2) {
    fontSize = '2rem';
  } else if (charCount <= 3) {
    fontSize = '1.4rem';
  } else if (charCount <= 4) {
    fontSize = '1rem';
  } else {
    fontSize = '0.8rem';
  }
  
  element.style.fontSize = fontSize;
}

/**
 * すべての駒のサイズを調整する関数
 * 盤面の駒、持ち駒、相手の持ち駒など、すべての駒に適用
 */
function adjustAllKomaFontSizes() {
  // 盤面の駒
  document.querySelectorAll('#board-container .cell.koma').forEach(element => {
    adjustKomaFontSize(element);
  });
  // 持ち駒
  document.querySelectorAll('#haveKoma-container .cell.koma').forEach(element => {
    adjustKomaFontSize(element);
  });
  // 相手の持ち駒
  document.querySelectorAll('#haveKoma_E-container .cell.koma').forEach(element => {
    adjustKomaFontSize(element);
  });
  // デッキ作成・編集画面の駒
  document.querySelectorAll('#board .cell.koma').forEach(element => {
    adjustKomaFontSize(element);
  });
}
