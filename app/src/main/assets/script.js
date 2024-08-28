
function showAndroidToast(toast) {
    Android.showToast(toast);
}

function openAndroidImage(img) {
    var pathImgUrl = img.src;
    Android.openDetailImage(pathImgUrl);
}