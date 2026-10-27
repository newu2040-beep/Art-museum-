const { execSync } = require('child_process');
const fs = require('fs');

const drawableDir = 'app/src/main/res/drawable';
if (!fs.existsSync(drawableDir)) {
  fs.mkdirSync(drawableDir, { recursive: true });
}

const img1 = 'public/assets/aistudio/39df5a3589deb73969362df918e2add5_505e8aa5.jpg';
const img2 = 'public/assets/aistudio/c476c2f65e88b68ad908c58a46d1f1c7_cc36636b.jpg';

// Helper to crop
function crop(src, x, y, w, h, dest) {
  const cmd = `convert "${src}" -crop ${w}x${h}+${x}+${y} +repage "${dest}"`;
  console.log(cmd);
  execSync(cmd);
}

// 1. Assets from Image 1 (736x736)
// char_autumn: character
crop(img1, 75, 45, 460, 690, `${drawableDir}/char_autumn.png`);

// plaid_tile: plaid swatch on top right
crop(img1, 598, 0, 138, 360, `${drawableDir}/plaid_tile.png`);

// paper_texture: clean watercolor paper texture from top
crop(img1, 150, 0, 200, 100, `${drawableDir}/paper_texture.png`);

// pumpkin: orange pumpkin on left
crop(img1, 0, 35, 105, 400, `${drawableDir}/pumpkin.png`);

// Doodles from Image 1:
// doodle_star:
crop(img1, 75, 5, 95, 95, `${drawableDir}/doodle_star.png`);
// doodle_heart:
crop(img1, 510, 220, 80, 80, `${drawableDir}/doodle_heart.png`);
// doodle_swirl:
crop(img1, 535, 65, 65, 145, `${drawableDir}/doodle_swirl.png`);
// doodle_leaf:
crop(img1, 640, 385, 75, 115, `${drawableDir}/doodle_leaf.png`);
// doodle_daisy:
crop(img1, 545, 640, 105, 85, `${drawableDir}/doodle_daisy.png`);

// 2. Sticker PNGs from Image 2 (675x1200)
// st_camera (Nikon)
crop(img2, 20, 20, 220, 210, `${drawableDir}/st_camera.png`);

// st_strawberry (Gingham strawberry)
crop(img2, 255, 20, 130, 165, `${drawableDir}/st_strawberry.png`);

// st_star_yellow (Yellow gingham star)
crop(img2, 15, 230, 200, 195, `${drawableDir}/st_star_yellow.png`);

// st_star_red (Red stitched star)
crop(img2, 225, 175, 170, 165, `${drawableDir}/st_star_red.png`);

// st_apple (Apple core)
crop(img2, 405, 200, 125, 120, `${drawableDir}/st_apple.png`);

// st_nametag (HELLO, I AM)
crop(img2, 18, 440, 145, 255, `${drawableDir}/st_nametag.png`);

// st_heart_denim (Denim heart with red border)
crop(img2, 175, 500, 185, 175, `${drawableDir}/st_heart_denim.png`);

// st_clock (Round clock)
crop(img2, 395, 505, 140, 135, `${drawableDir}/st_clock.png`);

// st_candy (Cherry candy wrapper)
crop(img2, 520, 700, 135, 165, `${drawableDir}/st_candy.png`);

// st_croissant (Croissant)
crop(img2, 15, 940, 215, 140, `${drawableDir}/st_croissant.png`);

// st_jam (Jam jar)
crop(img2, 240, 860, 150, 185, `${drawableDir}/st_jam.png`);

// st_bow (Green gingham bow)
crop(img2, 70, 1050, 145, 115, `${drawableDir}/st_bow.png`);

console.log('All drawables created successfully!');
