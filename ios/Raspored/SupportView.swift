import StoreKit
import SwiftUI

@MainActor
final class SupportStore: ObservableObject {
    @Published private(set) var products: [Product] = []
    @Published private(set) var loading = false
    @Published var message: String?

    private let ids = [
        "raspored_tip_small",
        "raspored_tip_medium",
        "raspored_tip_large"
    ]

    func load() async {
        guard products.isEmpty, !loading else { return }
        loading = true
        defer { loading = false }

        do {
            let fetched = try await Product.products(for: ids)
            products = fetched.sorted { lhs, rhs in
                (ids.firstIndex(of: lhs.id) ?? .max) <
                    (ids.firstIndex(of: rhs.id) ?? .max)
            }
            if products.isEmpty {
                message = "Opcije podrške još nisu aktivirane u App Store Connectu."
            }
        } catch {
            message = "Nije moguće učitati opcije podrške."
        }
    }

    func purchase(_ product: Product) async {
        do {
            let result = try await product.purchase()
            switch result {
            case .success(let verification):
                switch verification {
                case .verified(let transaction):
                    await transaction.finish()
                    message = "Hvala na dobrovoljnoj podršci!"
                case .unverified:
                    message = "Kupnja nije mogla biti potvrđena."
                }
            case .pending:
                message = "Kupnja čeka potvrdu."
            case .userCancelled:
                break
            @unknown default:
                message = "Kupnja nije dovršena."
            }
        } catch {
            message = "Kupnja nije dovršena."
        }
    }

    func label(for product: Product) -> String {
        switch product.id {
        case "raspored_tip_small":
            return "Mala podrška"
        case "raspored_tip_medium":
            return "Srednja podrška"
        case "raspored_tip_large":
            return "Velika podrška"
        default:
            return "Podrška"
        }
    }
}

struct SupportView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var store = SupportStore()

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [RColors.bg2, RColors.bg, .black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    Capsule()
                        .fill(RColors.muted.opacity(0.5))
                        .frame(width: 54, height: 5)
                        .frame(maxWidth: .infinity)

                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Dobrovoljna podrška")
                                .font(.system(size: 28, weight: .black))
                                .foregroundStyle(RColors.text)

                            Text("Podrška ne otključava funkcije i nije obavezna.")
                                .font(.subheadline)
                                .foregroundStyle(RColors.muted)
                        }

                        Spacer()

                        Button { dismiss() } label: {
                            Image(systemName: "xmark")
                                .font(.title3.bold())
                                .foregroundStyle(RColors.text)
                                .frame(width: 44, height: 44)
                                .background(RColors.card2)
                                .clipShape(Circle())
                        }
                        .buttonStyle(.plain)
                    }

                    if store.loading {
                        HStack(spacing: 10) {
                            ProgressView().tint(RColors.accent)
                            Text("Učitavanje opcija podrške…")
                                .foregroundStyle(RColors.muted)
                        }
                        .padding(.vertical, 12)
                    } else {
                        ForEach(store.products, id: \.id) { product in
                            Button {
                                Task { await store.purchase(product) }
                            } label: {
                                HStack {
                                    VStack(alignment: .leading, spacing: 3) {
                                        Text(store.label(for: product))
                                            .fontWeight(.bold)
                                            .foregroundStyle(RColors.text)
                                        Text("Jednokratna dobrovoljna podrška")
                                            .font(.caption2)
                                            .foregroundStyle(RColors.muted)
                                    }

                                    Spacer()

                                    Text(product.displayPrice)
                                        .fontWeight(.black)
                                        .foregroundStyle(RColors.accent)
                                }
                                .padding(14)
                                .background(RColors.card)
                                .clipShape(RoundedRectangle(cornerRadius: 18))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 18)
                                        .stroke(RColors.stroke, lineWidth: 1)
                                )
                                .shadow(color: .black.opacity(0.22), radius: 7, y: 3)
                            }
                            .buttonStyle(.plain)
                        }
                    }

                    if let message = store.message {
                        Text(message)
                            .font(.caption)
                            .foregroundStyle(RColors.muted)
                            .padding(.top, 4)
                    }

                    Text("Kupnju obrađuje App Store. Raspored ne prima niti sprema podatke o platnoj kartici.")
                        .font(.caption2)
                        .foregroundStyle(RColors.muted)
                        .padding(.top, 8)
                }
                .padding(18)
            }
        }
        .task { await store.load() }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.hidden)
    }
}
