/*
 * Copyright (C) 2015 47 Degrees, LLC http://47deg.com hello@47deg.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fortysevendeg.translatebubble.modules.notifications.impl

import android.app.{Notification, NotificationManager, PendingIntent}
import android.content.{Context, Intent}
import android.support.v4.app.NotificationCompat
import com.fortysevendeg.translatebubble.R
import com.fortysevendeg.translatebubble.commons.ContextWrapperProvider
import com.fortysevendeg.translatebubble.modules.notifications._
import com.fortysevendeg.translatebubble.modules.persistent.PersistentServicesComponent
import com.fortysevendeg.translatebubble.service.Service
import com.fortysevendeg.translatebubble.ui.preferences.MainActivity

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait NotificationsServicesComponentImpl
    extends NotificationsServicesComponent {

  self : PersistentServicesComponent with ContextWrapperProvider =>

  lazy val notificationsServices = new NotificationsServicesImpl

  class NotificationsServicesImpl
      extends NotificationsServices {

    private val NOTIFICATION_ID: Int = 1100

    val notifyManager = contextProvider.application.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]

    override def showTextTranslated: Service[ShowTextTranslatedRequest, ShowTextTranslatedResponse] = request =>
        Future {
          val notificationIntent: Intent = new Intent(contextProvider.application, classOf[MainActivity])
          val contentIntent: PendingIntent = PendingIntent.getActivity(contextProvider.application, getUniqueId, notificationIntent, 0)

          val builder = new NotificationCompat.Builder(contextProvider.application)
          val title = contextProvider.application.getString(R.string.translatedTitle, request.original)
          builder
              .setContentTitle(title)
              .setContentText(request.translated)
              .setTicker(title)
              .setContentIntent(contentIntent)
              .setSmallIcon(R.drawable.ic_notification_default)
              .setAutoCancel(true)

          if (persistentServices.isHeadsUpEnable()) {
            builder
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
          }

          val notification: Notification = new NotificationCompat.BigTextStyle(builder)
              .bigText(request.translated).build

          notifyManager.notify(NOTIFICATION_ID, notification)
          ShowTextTranslatedResponse()
        }

    override def failed(): Unit = {
      val notificationIntent: Intent = new Intent(contextProvider.application, classOf[MainActivity])
      val contentIntent: PendingIntent = PendingIntent.getActivity(contextProvider.application, getUniqueId, notificationIntent, 0)


      val builder = new NotificationCompat.Builder(contextProvider.application)
      val title = contextProvider.application.getString(R.string.failedTitle)
      val message = contextProvider.application.getString(R.string.failedMessage)
      val notification: Notification = builder
          .setContentTitle(title)
          .setContentText(message)
          .setTicker(title).setContentIntent(contentIntent)
          .setSmallIcon(R.drawable.ic_notification_default)
          .setAutoCancel(true)
          .build

      notifyManager.notify(NOTIFICATION_ID, notification)
    }

    def getUniqueId: Int = (System.currentTimeMillis & 0xfffffff).toInt

  }

}
