package server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Entity.Customer;
import Entity.CustomerModule;
import Entity.Employee;
import Entity.GasStationOrder;
import Entity.FuelPurchase;
import Entity.Message;
import Entity.MyFile;
import Entity.PricingModule;
import Entity.Rates;
import Entity.Sale;
import Entity.Supplier;
import boundery.ServerController;
import client.EmployeeCC;
import enums.Commands;
import ocsf.server.*;

public class MyFuelServer extends AbstractServer {

	public static ArrayList<UserOnline> usersOnline = new ArrayList<UserOnline>();
	public static String schemaName, dbPassword;

	private AnalticData analticData;
	private Thread thread;
	public MyFuelServer(int port) { 
		super(port);
	}

	// this.sendToAllClients(students[i].toString());

	public void handleMessageFromClient(Object msg, ConnectionToClient client) {
		Message message = (Message) msg;
		PricingModule pricingModel;
		ArrayList<PricingModule> result;
		
		File file;
		Object resltObject;
		
		ArrayList<String> arr;
		ArrayList<Object> str;
		
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat timeFileFormat = new SimpleDateFormat("HH-mm-ss");
		DateFormat timeDBFormat = new SimpleDateFormat("HH:mm:ss");
		
		boolean flag;
		
		
		ServerController.writeToServerConsole(
				"Message received: cmd " + message.getCmd() + " the object " + message.getObj() + " from " + client);

		switch (message.getCmd()) {
		case Login:
			arr = (ArrayList<String>) (message.getObj());
			resltObject = LogINController.LogIn(arr.get(0), arr.get(1));
			// founded a user
			if (resltObject != null) {
				if (resltObject instanceof Supplier) {
					Supplier supplier = (Supplier) resltObject;
					ServerController.onlineUserTableCont(supplier.getId(), "supplier");
					sendToClientObject(supplier, client);
				}
				if (resltObject instanceof Customer) {
					Customer customer = (Customer) resltObject;
					ServerController.onlineUserTableCont(customer.getId(), "customer");
					sendToClientObject(customer, client);
				}
				if (resltObject instanceof Employee) {
					Employee employee = (Employee) resltObject;
					ServerController.onlineUserTableCont(employee.getId(), employee.getRole());
					sendToClientObject(employee, client);
				}
			}
			sendToClientObject(resltObject, client);
			break;

		case logOut:
			arr = (ArrayList<String>) (message.getObj());
			String tableName = arr.get(1).toLowerCase().substring(13, arr.get(1).length());
			try {
				LogINController.updateUserOnlineStatus(tableName, arr.get(0), 0);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ServerController.onlineUserTableCont(arr.get(0), "");
			sendToClientObject("No Thing", client);
			break;
			
		case fastFuelingLogIn:
			String carNumber=(String)message.getObj();
			sendToClientArrayList(FastFuelController.fastFuelingLogIn(carNumber), client);
			break;
			
		case getAllCompanyFuelStationID:
			String companyName=(String)message.getObj();
				sendToClientArrayList(FastFuelController.getAllCompanyFuelStationID(companyName), client);
            break;
            
		case getPurchasePriceDetails:
			str =(ArrayList<Object>)message.getObj();
				sendToClientArrayList(FastFuelController.priceCalculationAndPricingModel
						((String)str.get(0),(CustomerModule)str.get(1),(int)str.get(2)), client);
            break;
            
		case commitFuelPurchase:
			str =(ArrayList<Object>)message.getObj();
			FuelPurchase fuelPurchase = (FuelPurchase)str.get(2);
			fuelPurchase.setTime(timeDBFormat.format(date));
			fuelPurchase.setDate(dateFormat.format(date));
			sendToClientObject(FastFuelController.commitFuelPurchase(
					(String)str.get(0),(String)str.get(1),fuelPurchase,(String)str.get(3)),client);
            break;
            
		case updatePricingModel:
			ArrayList<PricingModule> pricingModules = (ArrayList<PricingModule>) message.getObj();
			flag = true;
			for (PricingModule pricingModule : pricingModules) {
				if (!CompanyFuelControllerServer.updatePricingModelStatus(pricingModule))
					flag = false;
			}
			sendToClientObject(flag, client);
			break;
			
		case updateGasOrdersStatus:
			ArrayList<GasStationOrder> orders = (ArrayList<GasStationOrder>) message.getObj();
			flag = true;
			for (GasStationOrder order : orders) {
				if (!GasStationControllerServer.updateOrderStatus(order))
					flag = false;
			}
			sendToClientObject(flag, client);
			break;

		case updateSale:
			ArrayList<Sale> sales_updateSale = (ArrayList<Sale>) message.getObj();
			flag = true;
			for (Sale sale : sales_updateSale) {
				if (!CompanyFuelControllerServer.updateSale(sale))
					flag = false;
			}
			sendToClientObject(flag, client);
			break;

		case getAllCompanySalesByStatus:
			Sale salesPartialData = (Sale) message.getObj();
			// partial means that not all the data are filled
				ArrayList<Sale> sales_getAllCompanySalesByStatus = CompanyFuelControllerServer
						.getAllCompanySalesByStatus(salesPartialData.getCompanyName(), salesPartialData.getStatus());
				sendToClientArrayList(sales_getAllCompanySalesByStatus,client);

			break;

		case getAllCompanyRatesByStatus:
			pricingModel = (PricingModule) message.getObj();
			result = CompanyFuelControllerServer.getAllCompanyRatesByStatus(
					pricingModel.getCompanyName(), pricingModel.getStatus());
			sendToClientArrayList(result,client);
			break;

		case getCompanyPricingRate:
			pricingModel = (PricingModule) message.getObj();
			sendToClientObject(CompanyFuelControllerServer.getCompanyActivePricingRate(pricingModel.getCompanyName(), pricingModel.getModelNumber()),client);
			break;
			
		case savePricingModel:
			pricingModel = (PricingModule) message.getObj();
			sendToClientObject(CompanyFuelControllerServer.savePricingModel(pricingModel),client);
			break;

		case getSaleResponseReport:
			ArrayList<String> array = (ArrayList<String>) message.getObj();
			//salID,CompanyName 
			file=CompanyFuelControllerServer.responseReport(Integer.valueOf(array.get(0)), array.get(1), dateFormat.format(date), timeFileFormat.format(date));
			if(file!=null)
				try {
					client.sendToClient(new Message(convertFileToSeializable(file), Commands.reciveFile));
				} catch (IOException e) {
					e.printStackTrace();
				}
			else sendToClientObject(null, client);
			break;
		case getPeriodicReport:
			ArrayList<String> periodicReportdetails = (ArrayList<String>) message.getObj();
			file=CompanyFuelControllerServer.periodicReport(periodicReportdetails.get(0)
						,periodicReportdetails.get(1),periodicReportdetails.get(2),
						dateFormat.format(date), timeFileFormat.format(date));
			if(file!=null)
			try {
				client.sendToClient(new Message(convertFileToSeializable(file), Commands.reciveFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
			else sendToClientObject(null, client);
			break;
			
		case getAllCompanyFuel:
			String companyName1 = (String) message.getObj();
			sendToClientArrayList(CompanyFuelControllerServer.getAllCompanyFuelTypes(companyName1),client);
			break;
			
		case getCompanyFuel:
			ArrayList<String> compName = (ArrayList<String>) message.getObj();
			sendToClientObject(CompanyFuelControllerServer.getCompanyFuel(compName.get(0), compName.get(1)),client);
			break;
			
		case getAllOrdersByStatus:
			ArrayList<Object> objs = (ArrayList<Object>) message.getObj();
			String supplierId = (String)objs.get(0);
			String status =(String) objs.get(1);
		    sendToClientArrayList(GasStationControllerServer.getAllOrdersByStatus(supplierId,status),client);
			break;
			
		case GetMaxPrice:
			sendToClientObject(CompanyFuelController.getMaxPrice((String) message.getObj()),client);
			break;
			
		case CustomerOrderList:
			String s = (String) (message.getObj());
			sendToClientArrayList(MyOrderConrtollerServer.getOrders(s),client);
			break;
			
		default:
			System.out.println("default");
		}

	}
	
	/**
	 * Generic function that send to client any arrayList
	 * @param <T>
	 * @param obj
	 * @param client
	 */
	private <T> void sendToClientArrayList(ArrayList<T> obj,ConnectionToClient client) {
		try {
			if (obj != null) {
				if(!obj.isEmpty())
				client.sendToClient(new Message(obj, Commands.defaultRes));
				else 
					client.sendToClient(new Message(obj, Commands.NoElementFound));
			}
			//there was an expetion on the server that must be handeled 
			else client.sendToClient(new Message(null, Commands.NoElementFound));

		} catch (IOException e) {
			System.out.println("Cloud not send to client:"+client);
			e.printStackTrace();
		}
	}
	/**
	 * sends to the client single object from the arrayList
	 * @param <T>
	 * @param obj
	 * @param client
	 */
	private <T> void sendToClientObject(T obj,ConnectionToClient client) {
		try {
			if (obj != null)
				client.sendToClient(new Message(obj, Commands.defaultRes));
			else
				client.sendToClient(new Message(null, Commands.NoElementFound));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	/**
	 * convert File To Seializable
	 * @param fileName
	 * @param filePath
	 * @return
	 */
	private MyFile convertFileToSeializable(File file) {
		MyFile msg = new MyFile(file.getName());
		try {
			byte[] mybytearray = new byte[(int) file.length()];
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);

			msg.initArray(mybytearray.length);
			msg.setSize(mybytearray.length);

			bis.read(msg.getMybytearray(), 0, mybytearray.length);
		} catch (Exception e) {
			System.out.println("Error send ((Files)msg) to Server");
		}
		System.out.println("file had been converted");
		return msg;
	}
	
	protected void serverStarted() {
		ConnectionToDB.connectToDB(schemaName, dbPassword);
		// ServerController.writeToServerConsole("Server listening for connections on
		// port " + getPort());
		FileManagmentSys.createSystemWorkSpace();
		analticData = new AnalticData(); 
		thread = new Thread(analticData);
		thread.start();
		
		System.out.println("Server listening for connections on port " + getPort());
	}

	protected void serverStopped() {
		// ServerController.writeToServerConsole("Server has stopped listening for
	    // connections.");
		analticData.continueThread=false;
		try {
			thread.interrupt();
			thread.join(1000);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			ConnectionToDB.conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Server has stopped listening for connections.");
		//log out all the users
		/*
		for(UserOnline user : usersOnline) {
				try {
					if(user.userType.compareTo("customer")==0)
					LogINController.updateUserOnlineStatus("customer",user.getUserID(), 0);
					
					if(user.userType.compareTo("supplier")==0)
					LogINController.updateUserOnlineStatus("customer",user.getUserID(), 0);
					
					else 
					LogINController.updateUserOnlineStatus("customer",user.getUserID(), 0);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			ServerController.onlineUserTableCont(user.getUserID(),"");
		}
		*/
	}
	
	//just to show in the GUI
	/**
	 * String UserID ,String userType
	 * @author iamme
	 *
	 */
		public static class UserOnline{
			String UserID;
			String userType;
			/**
			 * 
			 * @param userID
			 * @param userType
			 */
			public UserOnline(String userID, String userType) {
				UserID = userID;
				this.userType = userType;
			}
			public String getUserID() {
				return UserID;
			}
			public void setUserID(String userID) {
				UserID = userID;
			}
			public String getUserType() {
				return userType;
			}
			public void setUserType(String userType) {
				this.userType = userType;
			}
			
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				UserOnline other = (UserOnline) obj;
				if (UserID == null) {
					if (other.UserID != null)
						return false;
				} else if (!UserID.equals(other.UserID))
					return false;
				return true;
			}
			@Override
			public String toString() {
				return "UserOnline [UserID=" + UserID + ", userType=" + userType + "]";
			}
			
		}
}