package com.mibr.android.intelligentreminder;

import com.mibr.android.intelligentreminder.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class INeedToPay extends Activity {
    public static ProgressDialog progressDialog;  
	private EditText ccNbr;
	private EditText expMonth;
	private EditText expYear;
	private EditText ccId;
	private EditText nameOnCard;
	private EditText city;
	private EditText state;
	private EditText postalCode;
	private EditText country;
	private EditText address;
	private String ccCardType;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
		INeedToo.mSingleton.doDatabaseCopy();

//        String uri = "market://details?id=com.mibr.android.intelligentreminder";
        String uri=getString(R.string.indeedtopay);
        Intent ii3=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(ii3);
		INeedToPay.this.finish();
		return;
		} catch (Exception e333v) {
			setContentView(R.layout.ineed2pay);
			setTitle(INeedToo.mSingleton.getHeading());
			Button submit = (Button) findViewById(R.id.ineed2pay_button_confirm);
			Button cancel = (Button) findViewById(R.id.ineed2pay_button_cancel);
			ccNbr=(EditText)findViewById(R.id.INeedToPayCCNbr);
			expMonth=(EditText)findViewById(R.id.INeedToPayExpirationMM);
			expYear=(EditText)findViewById(R.id.INeedToPayExpirationYYYY);
			ccId=(EditText)findViewById(R.id.INeedToPayccid);
			nameOnCard=(EditText)findViewById(R.id.INeedToPayNameOnCard);
			city=(EditText)findViewById(R.id.INeedToPayCity);
			state=(EditText)findViewById(R.id.INeedToPayState);
			postalCode=(EditText)findViewById(R.id.INeedToPayPostalCode);
			country=(EditText)findViewById(R.id.INeedToPayCountry);
			address=(EditText)findViewById(R.id.INeedToPayAddress);
	
			submit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ccCardType=null;
					String ccNbrValidation=INeedToPay.this.doCCValidationCriteria(ccNbr.getText().toString());
					String expMonthValidation=INeedToPay.this.doExpMonthValidation(expMonth.getText().toString());
					String expYearValidation=INeedToPay.this.doExpYearValidation(expYear.getText().toString(),expMonth.getText().toString());
					String ccIdValidation=INeedToPay.this.doCCIdValidation(ccId.getText().toString());
					String nameOnCardValidation=INeedToPay.this.doNameOnCard(nameOnCard.getText().toString());
					String cityValidation=INeedToPay.this.doCity(city.getText().toString());
					String stateValidation=INeedToPay.this.doState(state.getText().toString());
					String postalCodeValidation=INeedToPay.this.doPostalCode(postalCode.getText().toString());
					String countryValidation=INeedToPay.this.doCountry(country.getText().toString());
					String addressValidation=INeedToPay.this.doAddress(address.getText().toString());
					if (nameOnCardValidation != null) {
						AlertDialog.Builder builder2 = new AlertDialog.Builder(INeedToPay.this);
						builder2.setMessage(nameOnCardValidation)
						.setCancelable(false);
						builder2.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {		
							@Override
							public void onClick(DialogInterface dialog2,int id2) {
								nameOnCard.requestFocus();
							}
						});
						AlertDialog alert2=builder2.create();	
						alert2.show();
						return;
					}
					if (expMonthValidation != null) {
						AlertDialog.Builder builder2 = new AlertDialog.Builder(INeedToPay.this);
						builder2.setMessage(expMonthValidation)
						.setCancelable(false);
						builder2.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {		
							@Override
							public void onClick(DialogInterface dialog2,int id2) {
								expMonth.requestFocus();
							}
						});
						AlertDialog alert2=builder2.create();	
						alert2.show();
						return;
					}
					
					if(expYearValidation != null) {
						AlertDialog.Builder builder2 = new AlertDialog.Builder(INeedToPay.this);
						builder2.setMessage(expYearValidation)
						.setCancelable(false);
						builder2.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {		
							@Override
							public void onClick(DialogInterface dialog2,int id2) {
								expYear.requestFocus();
							}
						});
						AlertDialog alert2=builder2.create();	
						alert2.show();
						return;
					}
					if (ccIdValidation != null) {
						AlertDialog.Builder builder2 = new AlertDialog.Builder(INeedToPay.this);
						builder2.setMessage(ccIdValidation)
						.setCancelable(false);
						builder2.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {		
							@Override
							public void onClick(DialogInterface dialog2,int id2) {
								ccId.requestFocus();
							}
						});
						AlertDialog alert2=builder2.create();	
						alert2.show();
						return;
					}
					if (addressValidation != null) {
						AlertDialog.Builder builder2 = new AlertDialog.Builder(INeedToPay.this);
						builder2.setMessage(addressValidation)
						.setCancelable(false);
						builder2.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {		
							@Override
							public void onClick(DialogInterface dialog2,int id2) {
								address.requestFocus();
							}
						});
						AlertDialog alert2=builder2.create();	
						alert2.show();
						return;
					}
					if (cityValidation != null) {
						AlertDialog.Builder builder2 = new AlertDialog.Builder(INeedToPay.this);
						builder2.setMessage(cityValidation)
						.setCancelable(false);
						builder2.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {		
							@Override
							public void onClick(DialogInterface dialog2,int id2) {
								city.requestFocus();
							}
						});
						AlertDialog alert2=builder2.create();	
						alert2.show();
						return;
					}
					if (stateValidation != null) {
						AlertDialog.Builder builder2 = new AlertDialog.Builder(INeedToPay.this);
						builder2.setMessage(stateValidation)
						.setCancelable(false);
						builder2.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {		
							@Override
							public void onClick(DialogInterface dialog2,int id2) {
								state.requestFocus();
							}
						});
						AlertDialog alert2=builder2.create();	
						alert2.show();
						return;
					}
					if (postalCodeValidation != null) {
						AlertDialog.Builder builder2 = new AlertDialog.Builder(INeedToPay.this);
						builder2.setMessage(postalCodeValidation)
						.setCancelable(false);
						builder2.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {		
							@Override
							public void onClick(DialogInterface dialog2,int id2) {
								postalCode.requestFocus();
							}
						});
						AlertDialog alert2=builder2.create();	
						alert2.show();
						return;
					}
					if (countryValidation != null) {
						AlertDialog.Builder builder2 = new AlertDialog.Builder(INeedToPay.this);
						builder2.setMessage(countryValidation)
						.setCancelable(false);
						builder2.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {		
							@Override
							public void onClick(DialogInterface dialog2,int id2) {
								postalCode.requestFocus();
							}
						});
						AlertDialog alert2=builder2.create();	
						alert2.show();
						return;
					}
					if(ccNbrValidation.equals(String.valueOf(MASTERCARD))) {
						ccCardType="Mastercard";
					}
					if(ccNbrValidation.equals(String.valueOf(VISA))) {
						ccCardType="Visa";
					}
					if(ccNbrValidation.equals(String.valueOf(AMEX))) {
						ccCardType="Amex";
					}
					if(ccNbrValidation.equals(String.valueOf(DINERS))) {
						ccCardType="Diners";
					}
					if(ccNbrValidation.equals(String.valueOf(DISCOVER))) {
						ccCardType="Discover";
					}
					if(ccCardType==null) {
						AlertDialog.Builder builder = new AlertDialog.Builder(INeedToPay.this);
						builder.setMessage(ccNbrValidation)
						.setCancelable(false)
						.setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int id) {
								ccNbr.requestFocus();
							}
						});
						AlertDialog alert=builder.create();	
						alert.show();
						return;
					}
					new Thread(new Runnable() {
						public void run() {
					        INeedToPay.this.progressDialog = ProgressDialog.show(  
					                INeedToPay.this,  
					                "Registering Trial Version", // title  
					                "Please wait ... registering trial version", // message  
					                true // indeterminate  
					        );  
							
						}
					}).run();
					final Timer jdTimer = new Timer("Registering");
					jdTimer.schedule(new TimerTask() {
						public void run() {
							Intent intent=new Intent(INeedToPay.this,INeedWebService.class)
							.setAction("Register")
							.putExtra("ccNbr", ccNbr.getText().toString())
							.putExtra("expMonth", expMonth.getText().toString())
							.putExtra("expYear", expYear.getText().toString())
							.putExtra("nameOnCard", nameOnCard.getText().toString())
							.putExtra("ccCardType", ccCardType)
							.putExtra("city", city.getText().toString())
							.putExtra("state", state.getText().toString())
							.putExtra("postalCode", postalCode.getText().toString())
							.putExtra("country", country.getText().toString())
							.putExtra("address", address.getText().toString())
							.putExtra("ccId",ccId.getText().toString());
							startService(intent);
							jdTimer.cancel();
						}
					}, 500, 1000 * 60 * 10);
					
				}
			});
			cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i2;
					i2 = new Intent(INeedToPay.this, INeedToo.class);
					i2.putExtra("initialtabindex", (int)0);
					startActivity(i2);    
	
					INeedToPay.this.finish();
				}
			});
		}
	}
	/**
	 * A class for performing pre-validation on credit cards.
	 * 
	 * @author Michael Urban
	 */
	 
	    private static final int MASTERCARD = 0, VISA = 1;
	    private static final int AMEX = 2, DISCOVER = 3, DINERS = 4;
		
		private String doExpMonthValidation(String expMonth) {
			try {
				int month=Integer.valueOf(expMonth);
				if (month < 1 || month > 12) {
					return "Invalid Month";
				}
				return null;
			} catch (Exception eee3) {
				return "Invalid month";
			}
		}
		private String doExpYearValidation(String expYear, String expMonth) {
			int year=-1;
			int month=-1;
			try {
				year=Integer.valueOf(expYear);
				month=Integer.valueOf(expMonth);
				if (year < 2011 || year > 2025) {
					return "Invalid Year";
				}
			} catch (Exception eee3) {
				return "Invalid year";
			}
			try {
				GregorianCalendar g=new GregorianCalendar();
				int gYear=g.get(Calendar.YEAR);
				int gMonth=g.get(Calendar.MONTH)+1;
				if(
					(gYear > year) ||
					(gYear == year && gMonth>month)
					) {
					return "Expiration date is already passed";
				}
			} catch (Exception ee) {
				return "Invalid expiration date";
			}
			return null;
		}
		private String doCCIdValidation(String ccId) {
			if( ccId.trim().equals("")) {
				return "Please supply CC Id";
			} else {
				return null;
			}
		}
		private String doNameOnCard(String nameOnCard) {
			if (nameOnCard.trim().equals("")) {
				return "Please supply Name on Card";
			} else {
				return null;
			}
		}
		private String doCity(String city) {
			if (city.trim().equals("")) {
				return "Please supply City";
			} else {
				return null;
			}
		}
		private String doState(String state) {
			if (state.trim().equals("")) {
				return "Please supply State";
			} else {
				return null;
			}
		}
		private String doPostalCode(String pc) {
			if (pc.trim().equals("")) {
				return "Please supply Postal Code";
			} else {
				return null;
			}
		}
		private String doCountry(String cu) {
			if (cu.trim().equals("")) {
				return "Please supply Country";
			} else {
				return null;
			}
		}
		private String doAddress(String add) {
			if (add.trim().equals("")) {
				return "Please supply Address";
			} else {
				return null;
			}
		}


	    private String doCCValidationCriteria(String number) {
			
	        if (number.equals("")) {
	            return "Field cannnot be blank.";
	        }
			
	        Matcher m = Pattern.compile("[^\\d\\s.-]").matcher(number);
	        
	        if (m.find()) {
	            return "Credit card number can only contain numbers, spaces, \"-\", and \".\"";
	        }
			
	        int type = getCreditCardType(number);
	        if(type==-1) {
	            return "Invalid card number";
	        }
	        Matcher matcher = Pattern.compile("[\\s.-]").matcher(number);
	        number = matcher.replaceAll("");
	        if (!luhnValidate(number)) {
	            return "Invalid card number";
	        }
	        return String.valueOf(type);
	    }
	 
	    // Check that cards start with proper digits for
	    // selected card type and are also the right length.    
	 
	    private int getCreditCardType(String number) {
            if (!(number.length() != 16 ||
	                Integer.parseInt(number.substring(0, 2)) < 51 ||
	                Integer.parseInt(number.substring(0, 2)) > 55))
	            {
            	return MASTERCARD;
	        }
            if (!((number.length() != 13 && number.length() != 16) ||
	                    Integer.parseInt(number.substring(0, 1)) != 4))
	            {
	                return VISA;
	            }
				
            if (!(number.length() != 15 ||
	                (Integer.parseInt(number.substring(0, 2)) != 34 &&
	                    Integer.parseInt(number.substring(0, 2)) != 37)))
	            {
	                return AMEX;
	            }
				
            if (!(number.length() != 16 ||
	                Integer.parseInt(number.substring(0, 5)) != 6011))
	            {
	                return DISCOVER;
	            }
				
            if (!(number.length() != 14 ||
	                ((Integer.parseInt(number.substring(0, 2)) != 36 &&
	                    Integer.parseInt(number.substring(0, 2)) != 38) &&
	                    Integer.parseInt(number.substring(0, 3)) < 300 ||
	                        Integer.parseInt(number.substring(0, 3)) > 305)))
	            {
	                return DINERS;
	            }
	    	return -1;
	    }
	 
	    // The Luhn algorithm is basically a CRC type
	    // system for checking the validity of an entry.
	    // All major credit cards use numbers that will
	    // pass the Luhn check. Also, all of them are based
	    // on MOD 10.
		
	    private boolean luhnValidate(String numberString) {
	        char[] charArray = numberString.toCharArray();
	        int[] number = new int[charArray.length];
	        int total = 0;
			
	        for (int i=0; i < charArray.length; i++) {
	            number[i] = Character.getNumericValue(charArray[i]);
	        }
			
	        for (int i = number.length-2; i > -1; i-=2) {
	            number[i] *= 2;
				
	            if (number[i] > 9)
	                number[i] -= 9;
	        }
			
	        for (int i=0; i < number.length; i++)
	            total += number[i];
			
	            if (total % 10 != 0)
	                return false;
			
	        return true;
	    }
	}

