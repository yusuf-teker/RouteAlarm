import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
    var controller = MainViewControllerKt.MainViewController(
            platformMapFactory: IOSPlatformMapFactory.shared)
        
            controller.view.backgroundColor = .clear
              controller.setNeedsStatusBarAppearanceUpdate()
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
                .ignoresSafeArea(.container)
                .ignoresSafeArea()
    }
}



