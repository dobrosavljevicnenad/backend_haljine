function getCloudinaryPublicId(url) {
  try {
    const urlObj = new URL(url);
    const pathname = urlObj.pathname;
    const parts = pathname.split('/');

    const folder = parts[parts.length - 2];
    const filename = parts[parts.length - 1].split('.')[0];

    return `${folder}/${filename}`;
  } catch (err) {
    console.error('❌ Ne mogu da izvučem public_id iz URL-a:', err);
    return null;
  }
}

module.exports = getCloudinaryPublicId;
