package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Filled.Bookmark: ImageVector
    get() {
        if (_bookmark != null) return _bookmark!!
        _bookmark = materialIcon(name = "Filled.Bookmark") {
            materialPath {
                moveTo(17.0f, 3.0f)
                horizontalLineTo(7.0f)
                curveTo(5.9f, 3.0f, 5.0f, 3.9f, 5.0f, 5.0f)
                verticalLineTo(21.0f)
                lineTo(12.0f, 18.0f)
                lineTo(19.0f, 21.0f)
                verticalLineTo(5.0f)
                curveTo(19.0f, 3.9f, 18.1f, 3.0f, 17.0f, 3.0f)
                close()
            }
        }
        return _bookmark!!
    }
private var _bookmark: ImageVector? = null

val Icons.Filled.DeleteOutline: ImageVector
    get() {
        if (_deleteOutline != null) return _deleteOutline!!
        _deleteOutline = materialIcon(name = "Filled.DeleteOutline") {
            materialPath {
                moveTo(6.0f, 19.0f)
                curveTo(6.0f, 20.1f, 6.9f, 21.0f, 8.0f, 21.0f)
                horizontalLineTo(16.0f)
                curveTo(17.1f, 21.0f, 18.0f, 20.1f, 18.0f, 19.0f)
                verticalLineTo(7.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(8.0f, 9.0f)
                horizontalLineTo(16.0f)
                verticalLineTo(19.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(9.0f)
                close()
                moveTo(15.5f, 4.0f)
                lineTo(14.5f, 3.0f)
                horizontalLineTo(9.5f)
                lineTo(8.5f, 4.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(6.0f)
                horizontalLineTo(19.0f)
                verticalLineTo(4.0f)
                horizontalLineTo(15.5f)
                close()
            }
        }
        return _deleteOutline!!
    }
private var _deleteOutline: ImageVector? = null

val Icons.Filled.DeleteForever: ImageVector
    get() = Icons.Filled.DeleteOutline

val Icons.Filled.Flag: ImageVector
    get() {
        if (_flag != null) return _flag!!
        _flag = materialIcon(name = "Filled.Flag") {
            materialPath {
                moveTo(14.4f, 6.0f)
                lineTo(14.0f, 4.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(21.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(14.0f)
                horizontalLineTo(12.6f)
                lineTo(13.0f, 16.0f)
                horizontalLineTo(20.0f)
                verticalLineTo(6.0f)
                horizontalLineTo(14.4f)
                close()
            }
        }
        return _flag!!
    }
private var _flag: ImageVector? = null

val Icons.Filled.MoreHoriz: ImageVector
    get() {
        if (_moreHoriz != null) return _moreHoriz!!
        _moreHoriz = materialIcon(name = "Filled.MoreHoriz") {
            materialPath {
                moveTo(6.0f, 10.0f)
                curveTo(4.9f, 10.0f, 4.0f, 10.9f, 4.0f, 12.0f)
                curveTo(4.0f, 13.1f, 4.9f, 14.0f, 6.0f, 14.0f)
                curveTo(7.1f, 14.0f, 8.0f, 13.1f, 8.0f, 12.0f)
                curveTo(8.0f, 10.9f, 7.1f, 10.0f, 6.0f, 10.0f)
                close()
                moveTo(18.0f, 10.0f)
                curveTo(16.9f, 10.0f, 16.0f, 10.9f, 16.0f, 12.0f)
                curveTo(16.0f, 13.1f, 16.9f, 14.0f, 18.0f, 14.0f)
                curveTo(19.1f, 14.0f, 20.0f, 13.1f, 20.0f, 12.0f)
                curveTo(20.0f, 10.9f, 19.1f, 10.0f, 18.0f, 10.0f)
                close()
                moveTo(12.0f, 10.0f)
                curveTo(10.9f, 10.0f, 10.0f, 10.9f, 10.0f, 12.0f)
                curveTo(10.0f, 13.1f, 10.9f, 14.0f, 12.0f, 14.0f)
                curveTo(13.1f, 14.0f, 14.0f, 13.1f, 14.0f, 12.0f)
                curveTo(14.0f, 10.9f, 13.1f, 10.0f, 12.0f, 10.0f)
                close()
            }
        }
        return _moreHoriz!!
    }
private var _moreHoriz: ImageVector? = null
