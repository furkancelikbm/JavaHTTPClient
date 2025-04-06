package main;

import model.PaymentModel;
import view.POSAppView;
import viewmodel.POSViewModel;

public class POSApp {
    public static void main(String[] args) {
        PaymentModel model = new PaymentModel();
        POSViewModel viewModel = new POSViewModel(model);
        new POSAppView(viewModel);
    }
}
