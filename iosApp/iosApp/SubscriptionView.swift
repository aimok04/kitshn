import SwiftUICore
import SwiftUI
import StoreKit
import ComposeApp

struct SubscriptionView: View {
    @State private var showPurchaseAlert = false
    
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
            }
        }
        .alert(MainKt.lang(key: "ios_support_thanks"), isPresented: $showPurchaseAlert) {
            Button(MainKt.lang(key: "common_done"), role: .cancel) { }
        }
    }
}
