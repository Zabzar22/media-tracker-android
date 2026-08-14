package edu.metrostate.ics342.mediatracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.theme.FinishedContainer
import edu.metrostate.ics342.mediatracker.theme.Finished
import edu.metrostate.ics342.mediatracker.theme.InProgressContainer
import edu.metrostate.ics342.mediatracker.theme.InProgress
import edu.metrostate.ics342.mediatracker.theme.WantToContainer
import edu.metrostate.ics342.mediatracker.theme.WantTo

/**
 * The Want To / In Progress / Finished status pill from the wireframes.
 * Each status gets its own light container background with the matching saturated text color.
 */
@Composable
fun StatusBadge(
    status: LibraryStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val background: Color
    val textColor: Color
    when (status) {
        LibraryStatus.WANT_TO     -> { background = WantToContainer;     textColor = WantTo }
        LibraryStatus.IN_PROGRESS -> { background = InProgressContainer; textColor = InProgress }
        LibraryStatus.FINISHED    -> { background = FinishedContainer;   textColor = Finished }
    }

    Text(
        text  = stringResource(status.labelRes),
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
