public class POSViewModel {
    private PaymentModel model;

    public POSViewModel(PaymentModel model) {
        this.model = model;
    }

    public String processPayment() {
        // Call the model's processPayment method and return the result
        if (model.processPayment()) {
            return "Ödeme alındı!";
        } else {
            return "Ödeme başarısız!";
        }
    }
}
