package com.bbaf.mpos.inventory.ui;

import java.util.ArrayList;

import com.bbaf.mpos.R;
import com.bbaf.mpos.FacadeController.Register;
import com.bbaf.mpos.FacadeController.Store;
import com.bbaf.mpos.ProductDescription.ProductDescription;
import com.bbaf.mpos.R.layout;
import com.bbaf.mpos.R.menu;
import com.bbaf.mpos.sale.SaleLineItem;
import com.bbaf.mpos.sale.ui.SaleTableHead;
import com.bbaf.mpos.sale.ui.SaleTableRow;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class InventoryActivity2 extends Activity {

	private TabHost tabHost;

	private TabSpec tabSale;
	//private TextView textViewStatus; Removed
	private TextView textViewTotalPriceText;
	private Button buttonAddItem;
	private Button buttonScan;
	private TableLayout tableLayoutSale;
	private EditText editTextInputID;
	private EditText editTextQuantity;
	private Button buttonPayment; // name changed from buttonSubmit
	private Button buttonCancelSale; // name changed from buttonRemoveSale
	
	private TabSpec tabInventory;
	private TableLayout tableLayoutInventory;
	private Button buttonAddProduct;
	private Button buttonATS; // name changed from buttonEdit
	private Button buttonRemoveProduct;
	

	// bat: maybe collect at same location later
	private static final int ADD_ACTIVITY_REQUESTCODE = 0;
	private static final int EDIT_ACTIVITY_REQUESTCODE = 1;
	private static final int SCANNER_ACTIVITY_REQUESTCODE = 49374;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inventory_activity2);
		
		tabHost = (TabHost) findViewById(R.id.tabhost2);
		tabHost.setup();

		// Tab Sale
		tabSale = tabHost.newTabSpec("tabSale");
		tabSale.setContent(R.id.tabSale);
		tabSale.setIndicator("Sale");
		tabHost.addTab(tabSale);
		
		//textViewStatus = (TextView)findViewById(R.id.textViewStatus);
		textViewTotalPriceText = (TextView)findViewById(R.id.textViewTotalPriceText);
		
		buttonAddItem = (Button)findViewById(R.id.buttonAddItem);
		buttonAddItem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String id = editTextInputID.getText().toString();
				if (id.equals("")) {
					Toast.makeText(getApplicationContext(), "ID must not be empty.", Toast.LENGTH_SHORT).show();
				}
				else {
					ProductDescription product = Store.getInstance().getProduct(id);
					if (product == null) {
						Toast.makeText(getApplicationContext(), "ID has not registered.", Toast.LENGTH_SHORT).show();
					}
					else {
						String quantityText = editTextQuantity.getText().toString();
						int quantity = quantityText.equals("") ? 1 : Integer.parseInt(quantityText);
						int tmp = 0;
						if (Register.getInstance().getSaleLineItemList(product) != null)
							tmp = Register.getInstance().getSaleLineItemList(product).getQuantity();
						//int stock = inventory.getQuantity(id) - tmp;
						
//						if (stock < quantity) {
//							Toast.makeText(getApplicationContext(), "ProductId: " + product.getId() + " has only " + stock + ".", Toast.LENGTH_SHORT).show();
//						}
//						else {
							if (Register.getInstance().addItem(product, quantity)) {
								// not sure it should be here
								Toast.makeText(getApplicationContext(), "ProductId: " + product.getId() + " is added successfully.", Toast.LENGTH_SHORT).show();
								refreshSaleTable();
								
								String status = product.getName() + " : " + quantity + " = " + quantity*product.getPrice() + " Bht.";
								//textViewStatus.setText(status);
								
							}
							else {
								Toast.makeText(getApplicationContext(), "Adding not successful.", Toast.LENGTH_SHORT).show();
							}
//						}
					}
				}
			}
		});
		
		tableLayoutSale = (TableLayout)findViewById(R.id.tableLayoutSale);
		SaleTableHead tableHead = new SaleTableHead(this);
		tableLayoutSale.addView(tableHead);
		
		buttonScan = (Button)findViewById(R.id.buttonScan3);
		buttonScan.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		editTextInputID = (EditText)findViewById(R.id.editTextInputID);
		editTextQuantity = (EditText)findViewById(R.id.editTextQuantity);
		
		buttonPayment = (Button)findViewById(R.id.buttonPayment);
		buttonPayment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ArrayList<SaleLineItem> sli = Register.getInstance().getAllSaleLineItemList();
				for(int i = 0 ;i < sli.size();i++){
					Register.getInstance().decrease(sli.get(i).getProductDescription().getId(), sli.get(i).getQuantity());
				}
				
				if (sli.size() != 0) {
					Register.getInstance().getLedger().record(Register.getInstance().getSale());
					Toast.makeText(getApplicationContext(), "Sale ended with " + sli.size() + " line item(s).", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getApplicationContext(), "Sale cancelled ", Toast.LENGTH_SHORT).show();
				}

				Register.getInstance().endSale();
				Register.getInstance().startSale();
				
				clearSaleTab();
			}
		});
		
		buttonCancelSale = (Button)findViewById(R.id.buttonCancelSale);
		buttonCancelSale.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Register.getInstance().removeAllItem();
				refreshIntenvoryTable();
				//textViewStatus.setText("Welcome");
				textViewTotalPriceText.setText("0.0");
				editTextInputID.setText("");
				editTextQuantity.setText("");
			}
		});
		
		
		// Tab Inventory
		tabInventory = tabHost.newTabSpec("tabInventory");
		tabInventory.setContent(R.id.tabInventory2);
		tabInventory.setIndicator("Inventory");
		tabHost.addTab(tabInventory);
		
		tableLayoutInventory = (TableLayout)findViewById(R.id.tableLayoutInventory);
		
		buttonAddProduct = (Button)findViewById(R.id.buttonAddProduct);
		buttonAddProduct.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent addActivity = new Intent(getApplicationContext(), AddProductActivity.class);
				startActivityForResult(addActivity, ADD_ACTIVITY_REQUESTCODE/*ADD_ACTIVITY_REQUESTCODE is 0*/);
				
			}
		});
		
		buttonATS = (Button)findViewById(R.id.buttonAddToSale);
		buttonATS.setOnClickListener(new ATSOnClickListener(tableLayoutInventory, this));
		
		//TODO change activity to add to sale...
		//buttonATS.setOnClickListener(new EditOnClickListener(tableLayoutInventory, this));
		
		buttonRemoveProduct = (Button)findViewById(R.id.buttonRemoveProduct);
		buttonRemoveProduct.setOnClickListener(new RemoveOnClickListener(tableLayoutInventory, this));
		
		
		
		Register.getInstance().startSale();
		refreshIntenvoryTable();
	}
	
	public void refreshIntenvoryTable() {
		tableLayoutInventory.removeAllViews();
		tableLayoutInventory.addView(new InventoryTableHead(this));

		ArrayList<ProductDescription> productList = Register.getInstance().getInventory().getAllProduct();
		
		if (productList != null) {
			if (productList.size() == 0) {
				TableRow free = new TableRow(this);
				TextView c = new TextView(this);
				free.addView(c);
				TextView v = new TextView(this);
				v.setText("Empty");
				free.addView(v);
				tableLayoutInventory.addView(free);
				return;
			}
			for (int i = 0; i < productList.size(); i++) {
				ProductDescription product = productList.get(i);
				String id = product.getId();
				int quantity = Store.getInstance().getQuantity(id);

				InventoryTableRow row = new InventoryTableRow(this,
						productList.get(i), quantity);
				tableLayoutInventory.addView(row);
			}
		}
	}
	
	public void refreshSaleTable() {
		tableLayoutSale.removeAllViews();
		tableLayoutSale.addView(new SaleTableHead(this));

		ArrayList<SaleLineItem> saleLineItem = Register.getInstance().getAllSaleLineItemList();

		if (saleLineItem != null) {
			if (saleLineItem.size() == 0) {
				TableRow free = new TableRow(this);
				TextView c = new TextView(this);
				free.addView(c);
				TextView v = new TextView(this);
				v.setText("Empty");
				free.addView(v);
				tableLayoutSale.addView(free);
				return;
			}
			for (int i = 0; i < saleLineItem.size(); i++) {
				ProductDescription product = saleLineItem.get(i).getProductDescription();
				double unitPrice = product.getPrice();
				int quantity = saleLineItem.get(i).getQuantity();
				String id = product.getId();

				SaleTableRow row = new SaleTableRow(this,
						product, unitPrice, quantity);
				tableLayoutSale.addView(row);
			}
			
			textViewTotalPriceText.setText(String.valueOf(Register.getInstance().getTotal()));
		}
	}
	
	public void clearSaleTab() {
		refreshSaleTable();
		editTextInputID.setText("");
		editTextQuantity.setText("");
		//textViewStatus.setText("welcome");
		textViewTotalPriceText.setText("0.0");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// bat: for tracking whether from which Activity and what is the result
		Log.d("result", "requestCode " + requestCode);
		Log.d("result", "resultCode " + resultCode);
		//
		if (requestCode == ADD_ACTIVITY_REQUESTCODE) {
			/**
			 * 0 = ADD_CANCEL 1 = ADD_SUCCESS
			 */
			if (resultCode == 0) {
				// no need to refresh
			} else if (resultCode == 1) {
				refreshIntenvoryTable();
			}
		}
		else if (requestCode == EDIT_ACTIVITY_REQUESTCODE) {
			/**
			 * 0 = EDIT_CANCEL 1 = EDIT_SUCCESS
			 */
			if (resultCode == 0) {
				// no need to refresh
			} else if (resultCode == 1) {
				refreshIntenvoryTable();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.inventory_activity2, menu);
		return true;
	}

}
