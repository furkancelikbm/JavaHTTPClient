
public class POSApp {
    public static void main(String[] args) {
        // Create the Model
        PaymentModel model = new PaymentModel();

        // Create the ViewModel and bind it to the Model
        POSViewModel viewModel = new POSViewModel(model);

        // Create the View and pass the ViewModel to it
        POSAppView view = new POSAppView(viewModel);
    }
}
