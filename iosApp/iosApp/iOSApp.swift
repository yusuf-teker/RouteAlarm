import SwiftUI
import GoogleMaps
import GooglePlaces
import UserNotifications


@main
struct iOSApp: App {
    init(){
        requestNotificationPermission()

        if let apiKey = Bundle.main.object(forInfoDictionaryKey: "GOOGLE_API_KEY") as? String {
        GMSServices.provideAPIKey(apiKey)
        GMSPlacesClient.provideAPIKey(apiKey)
    } else {
        fatalError("Google API key not found in Info.plist")
    }

    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

func requestNotificationPermission() {
    UNUserNotificationCenter
        .current()
        .requestAuthorization(options: [.alert, .sound, .badge]) { (success, error) in
            if success {
                print("Notification permission granted.")

                // İzin alındıktan sonra notification settings'i kontrol et
                UNUserNotificationCenter.current().getNotificationSettings { settings in
                    print("Notification settings: \(settings)")
                    print("Authorization status: \(settings.authorizationStatus.rawValue)")
                    print("Alert setting: \(settings.alertSetting.rawValue)")
                }
            } else if let error = error {
                print("Notification permission error: \(error.localizedDescription)")
            }
        }
}
