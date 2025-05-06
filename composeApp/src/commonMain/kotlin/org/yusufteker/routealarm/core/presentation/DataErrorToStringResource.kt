
package org.yusufteker.routealarm.core.presentation

import org.yusufteker.routealarm.core.domain.DataError
import routealarm.composeapp.generated.resources.Res
import routealarm.composeapp.generated.resources.error_disk_full
import routealarm.composeapp.generated.resources.error_local_unknown
import routealarm.composeapp.generated.resources.error_no_internet
import routealarm.composeapp.generated.resources.error_remote_unknown
import routealarm.composeapp.generated.resources.error_request_timeout
import routealarm.composeapp.generated.resources.error_serialization
import routealarm.composeapp.generated.resources.error_server
import routealarm.composeapp.generated.resources.error_too_many_requests


fun DataError.toUiText(): UiText {

    val stringResource = when (this) {

        DataError.Local.DISK_FULL -> Res.string.error_disk_full
        DataError.Local.UNKOWN -> Res.string.error_local_unknown
        DataError.Remote.REQUEST_TIMEOUT -> Res.string.error_request_timeout
        DataError.Remote.TOO_MANY_REQUESTS -> Res.string.error_too_many_requests
        DataError.Remote.NO_INTERNET -> Res.string.error_no_internet
        DataError.Remote.SERVER -> Res.string.error_server
        DataError.Remote.SERIALIZATION -> Res.string.error_serialization
        DataError.Remote.UNKNOWN -> Res.string.error_remote_unknown



    }

    return UiText.StringResourceId(stringResource)
}