import SwiftUICore
import SwiftUI
import StoreKit
import ComposeApp
import SafariServices

struct SubscriptionView: View {
    @State private var showPurchaseAlert = false
    @State private var showLearnMoreSheet = false
    
    var body: some View {
        VStack {
            if #available(iOS 17.0, *) {
                SubscriptionStoreView(groupID: SUPPORT_SUBSCRIPTION_GROUP_ID)
                    .onInAppPurchaseCompletion { product, result in
                        if case .success(.success(let transaction)) = result {
                            showPurchaseAlert = true
                            MainKt.handleSubscriptionChange(isSubscribed: true)
                        }
                    }
                
                Button(MainKt.lang(key: "common_learn_more")) {
                    showLearnMoreSheet = true
                }
            }
        }
        .alert(MainKt.lang(key: "ios_support_thanks"), isPresented: $showPurchaseAlert) {
            Button(MainKt.lang(key: "common_done"), role: .cancel) { }
        }
        .sheet(isPresented: $showLearnMoreSheet) {
            SafariWebView(url: URL(string: "https://kitshn.app/funding")!)
        }
    }
}

struct SafariWebView: UIViewControllerRepresentable {
    let url: URL
    
    func makeUIViewController(context: Context) -> some UIViewController {
        return SFSafariViewController(url: url)
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        
    }
}
