const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// function to notify anonymous users when a champion comes online
exports.sendAdminNotification = functions.database.ref('/Users/{pushId}')
    .onUpdate((change, context) => {
      // check if status was offline previously
      const beforeUser = change.before.val();
      const beforeStatus = beforeUser.status;
      if (beforeStatus==="offline") {
        // check if status is online now
        const afterUser = change.after.val();
        const afterStatus = afterUser.status;
        if (afterStatus==="online") {
          // build notification
          const payload = {
            notification: {
              title: 'Champion online!',
              body: `${afterUser.username}`
            }
          };

          // send notifications to topic subscribers
          return admin.messaging()
            .sendToTopic("anonymousUsers",payload)
            .then(response => {
              return console.log('Notification sent successfully', response);
            })
            .catch(error => {
              console.log('Notification sent failed:',error);
            });
        }
      }

    // no notification sent
    console.log('No change from Offline to Online, notification not sent');
    return null;
});
