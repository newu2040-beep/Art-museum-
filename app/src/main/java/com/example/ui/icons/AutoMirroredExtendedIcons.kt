package androidx.compose.material.icons.automirrored.outlined

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.AutoMirrored.Outlined.Logout: ImageVector
    get() {
        if (_logout != null) return _logout!!
        _logout = materialIcon(name = "AutoMirrored.Outlined.Logout", autoMirror = true) {
            materialPath {
                moveTo(17.0f, 7.0f)
                lineTo(15.59f, 8.41f)
                lineTo(18.17f, 11.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(13.0f)
                horizontalLineTo(18.17f)
                lineTo(15.59f, 15.58f)
                lineTo(17.0f, 17.0f)
                lineTo(22.0f, 12.0f)
                lineTo(17.0f, 7.0f)
                close()
                moveTo(4.0f, 5.0f)
                horizontalLineTo(12.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 3.0f, 2.0f, 3.9f, 2.0f, 5.0f)
                verticalLineTo(19.0f)
                curveTo(2.0f, 20.1f, 2.9f, 21.0f, 4.0f, 21.0f)
                horizontalLineTo(12.0f)
                verticalLineTo(19.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(5.0f)
                close()
            }
        }
        return _logout!!
    }
private var _logout: ImageVector? = null
