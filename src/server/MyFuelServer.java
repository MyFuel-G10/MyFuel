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

import Entity.Car;
import Entity.CompanyFuel;
import Entity.Customer;
import Entity.CustomerModule;
import Entity.Employee;
import Entity.GasOrder;
import Entity.Fuel;
import Entity.GasStationOrder;
import Entity.GenericReport;
import Entity.FuelPurchase;
import Entity.Message;
import Entity.MyFile;
import Entity.PricingModule;
import Entity.Rates;
import Entity.Sale;
import Entity.StationFuel;
import Entity.Supplier;
import boundery.ServerController;
import boundery.SupplierController;
import client.EmployeeCC;
import enums.Commands;
import enums.Quarter;
import enums.StationManagerReportsTypes;
import ocsf.server.*;
import server.GasStationControllerServer;

/**
 * The Class MyFuelServer controls all the my fuel system buttons and users.
 */
public class MyFuelServer extends AbstractServer {

	/** The users online. */
	public static ArrayList<UserOnline> usersOnline = new ArrayList<UserOnline>();
	
	/** The db password. */
	public static String schemaName, dbPassword;

	/** The analtic data. */
	private AnalticData analticData;
	
	/** The thread. */
	private Thread thread;
	
	/** The auto matic purchase model 3 calc. */
	//
	private AutoMaticPurchaseModel3Calc autoMaticPurchaseModel3Calc;
	
	/** The sec thread. */
	private Thread secThread;

	/** The orders. */
	ArrayList<GasStationOrder> orders;
	
	/** The status. */
	String status;

	/**
	 * Instantiates a new my fuel server.
	 *
	 * @param port the port
	 */
	public MyFuelServer(int port) {
		super(port);
	}

	// this.sendToAllClients(students[i].toString());

	/**
	 * Handle message from client.
	 *
	 * @param msg the msg
	 * @param client the client
	 */
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
		int stationID;

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

			switch (tableName) {
			case "customer":
				break;
			case "supplier":
				break;
			default:
				tableName = "employee";
			}

			try {
				LogINController.updateUserOnlineStatus(tableName, arr.get(0), 0);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ServerController.onlineUserTableCont(arr.get(0), "");
			sendToClientObject("No Thing", client);
			break;

		case fastFuelingLogIn:
			String carNumber = (String) message.getObj();
			sendToClientArrayList(FastFuelController.fastFuelingLogIn(carNumber), client);
			break;

		case getAllCompanyFuelStationID:
			String companyName = (String) message.getObj();
			sendToClientArrayList(FastFuelController.getAllCompanyFuelStationID(companyName), client);
			break;

		case getPurchasePriceDetails:
			str = (ArrayList<Object>) message.getObj();
			sendToClientArrayList(FastFuelController.priceCalculationAndPricingModel((String) str.get(0),
					(String) str.get(1), (String) str.get(2), (int) str.get(3), (int) str.get(4),
					dateFormat.format(date), timeDBFormat.format(date)), client);
			break;

		case commitFuelPurchase:
			str = (ArrayList<Object>) message.getObj();
			FuelPurchase fuelPurchase = (FuelPurchase) str.get(3);
			fuelPurchase.setTime(timeDBFormat.format(date));
			fuelPurchase.setDate(dateFormat.format(date));

			sendToClientObject(FastFuelController.commitFuelPurchase((String) str.get(0), (int) str.get(1),
					(String) str.get(2), fuelPurchase, (String) str.get(4)), client);
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
			sendToClientArrayList(sales_getAllCompanySalesByStatus, client);

			break;

		case getAllCompanyRatesByStatus:
			pricingModel = (PricingModule) message.getObj();
			result = CompanyFuelControllerServer.getAllCompanyRatesByStatus(pricingModel.getCompanyName(),
					pricingModel.getStatus());
			sendToClientArrayList(result, client);
			break;

		case getCompanyPricingRate:
			pricingModel = (PricingModule) message.getObj();
			sendToClientObject(CompanyFuelControllerServer.getCompanyActivePricingRate(pricingModel.getCompanyName(),
					pricingModel.getModelNumber()), client);
			break;

		case savePricingModel:
			pricingModel = (PricingModule) message.getObj();
			sendToClientObject(CompanyFuelControllerServer.savePricingModel(pricingModel), client);
			break;

		case getSaleResponseReport:
			ArrayList<String> array = (ArrayList<String>) message.getObj();
			// salID,CompanyName
			file = CompanyFuelControllerServer.responseReport(Integer.valueOf(array.get(0)), array.get(1),
					dateFormat.format(date), timeFileFormat.format(date));
			if (file != null)
				try {
					client.sendToClient(new Message(convertFileToSeializable(file), Commands.reciveFile));
				} catch (IOException e) {
					e.printStackTrace();
				}
			else
				sendToClientObject(null, client);
			break;
		case getPeriodicReport:
			ArrayList<String> periodicReportdetails = (ArrayList<String>) message.getObj();
			file = CompanyFuelControllerServer.periodicReport(periodicReportdetails.get(0),
					periodicReportdetails.get(1), periodicReportdetails.get(2), dateFormat.format(date),
					timeFileFormat.format(date));
			if (file != null)
				try {
					client.sendToClient(new Message(convertFileToSeializable(file), Commands.reciveFile));
				} catch (IOException e) {
					e.printStackTrace();
				}
			else
				sendToClientObject(null, client);
			break;

		case getAllCompanyFuel:
			String companyName1 = (String) message.getObj();
			sendToClientArrayList(CompanyFuelControllerServer.getAllCompanyFuelTypes(companyName1), client);
			break;

		case updateFuelMaxPriceDetails:
			ArrayList<CompanyFuel> list = (ArrayList<CompanyFuel>) message.getObj();
			try {
				client.sendToClient(new Message(CompanyFuelControllerServer.updateAllFuelMaxPriceDetails(list),
						Commands.defaultRes));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case getFuelMaxPriceDetails:
			String companyName2 = (String) (message.getObj());
			try {
				client.sendToClient(new Message(CompanyFuelControllerServer.getAllFuelMaxPriceDetails(companyName2),
						Commands.defaultRes));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case getCompanyFuel:
			ArrayList<String> compName = (ArrayList<String>) message.getObj();
			sendToClientObject(CompanyFuelControllerServer.getCompanyFuel(compName.get(0), compName.get(1)), client);
			break;

		case getAllOrdersByStatus:
			ArrayList<Object> objs = (ArrayList<Object>) message.getObj();
			String supplierId = (String) objs.get(0);
			String status = (String) objs.get(1);
			sendToClientArrayList(GasStationControllerServer.getAllOrdersByStatus(supplierId, status), client);
			break;

		case GetMaxPrice:
			sendToClientObject(CompanyFuelController.getMaxPrice((String) message.getObj()), client);
			break;

		case getBuildRateApprovalDetails:
			String companyName3 = (String) (message.getObj());
			try {
				client.sendToClient(new Message(CompanyFuelControllerServer.getBuildRateApprovalDetails(companyName3),
						Commands.defaultRes));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case CustomerOrderList:
			String s = (String) (message.getObj());
			sendToClientArrayList(CustomerGasOrderController.getOrders(s), client);
			break;

		case CreateNewOrder:
			boolean orderObj = CustomerGasOrderController.createNewOrder((GasOrder) message.getObj());
			sendToClientObject(orderObj, client);
			break;

		case addNewCustomer:
			Customer cust = (Customer) (message.getObj());
			sendToClientObject(EmployeeController.addNewCustmer(cust), client);
			break;

		case checkIfExist:
			String id = (String) (message.getObj());
			sendToClientObject(EmployeeController.checkIfExist(id), client);
			break;

		case addNewCar:
			Car car = (Car) (message.getObj());
			sendToClientObject(EmployeeController.addNewCar(car), client);
			break;

		case updateModels:
			ArrayList<Object> objlist = (ArrayList<Object>) message.getObj();
			String pricing = (String) objlist.get(0);
			String purchase = (String) objlist.get(1);
			String cuId = (String) objlist.get(2);

			sendToClientObject(EmployeeController.updateModels(pricing, purchase, cuId), client);
			break;

		case getCustomerDetails:
			String upid = (String) (message.getObj());
			sendToClientObject(EmployeeController.getCustomerDetails(upid), client);
			break;

		case updateCustomerDetails:
			ArrayList<String> update = (ArrayList<String>) (message.getObj());
			sendToClientObject(EmployeeController.updateCustomerDetails(update), client);
			break;

		case getAllSales:
			sendToClientArrayList(EmployeeController.getAllSales(), client);
			break;

		case deleteSales:
			ArrayList<Sale> sales = (ArrayList<Sale>) message.getObj();
			flag = true;
			for (Sale sale : sales) {
				if (!EmployeeController.deleteSales(sale))
					flag = false;
			}

			sendToClientObject(flag, client);
			break;

		case addNewSaleTemp:
			Sale sale = (Sale) message.getObj();
			sendToClientObject(EmployeeController.addNewSaleTemp(sale), client);
			break;

		case getAllstationOrdersBystatus:
			ArrayList<Object> o = (ArrayList<Object>) message.getObj();
			int stationid = (int) o.get(0);
			status = (String) o.get(1);
			sendToClientArrayList(GasStationControllerServer.getAllstationOrdersByStatus(stationid, status), client);
			break;

		case approveOrders:
			orders = (ArrayList<GasStationOrder>) message.getObj();
			sendToClientObject(GasStationControllerServer.changeOrdersStatus(orders), client);
			break;

		case getAllStationFuelById:

			int stationId = (Integer) message.getObj();
			try {
				client.sendToClient(
						new Message(GasStationControllerServer.getAllStationFuel(stationId), Commands.defaultRes));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case updateFuelMinQuantitybyType:
			ArrayList<Object> k = (ArrayList<Object>) message.getObj();

			try {
				client.sendToClient(new Message(GasStationControllerServer.updateFuelMinQuantitybyType((int) k.get(0),
						(String) k.get(1), (float) k.get(2)), Commands.defaultRes));
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case createFuelStationIncmomeReport:

			ArrayList<Object> params = (ArrayList<Object>) message.getObj();
			int st_id = (int) params.get(0);
			String companyname = (String) params.get(1);
			Quarter q = (Quarter) params.get(2);
			String year = (String) params.get(3);
			// File file;
			try {
				file = GasStationControllerServer.createFuelStationIncmomeReport(st_id, companyname, q, year);

				if (file == null)
					client.sendToClient(new Message(null, Commands.defaultRes));

				else
					client.sendToClient(new Message(convertFileToSeializable(file), Commands.reciveFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case createFuelStationPurchasesReport:
			ArrayList<Object> params1 = (ArrayList<Object>) message.getObj();
			int st_id1 = (int) params1.get(0);
			String companyname1 = (String) params1.get(1);
			Quarter q1 = (Quarter) params1.get(2);
			String year1 = (String) params1.get(3);
			File file2;

			try {
				file2 = GasStationControllerServer.createFuelStationPurchasesReport(st_id1, companyname1, q1, year1);
				if (file2 == null)
					client.sendToClient(new Message(null, Commands.defaultRes));

				else
					client.sendToClient(new Message(convertFileToSeializable(file2), Commands.reciveFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case createFuelStationInventoryReport:
			ArrayList<Object> params2 = (ArrayList<Object>) message.getObj();
			int st_id2 = (int) params2.get(0);
			String companyname2 = (String) params2.get(1);
			Quarter q2 = (Quarter) params2.get(2);
			String year2 = (String) params2.get(3);
			File file3;

			try {
				file3 = GasStationControllerServer.createInventoryReporteport(st_id2, companyname2, q2, year2);
				if (file3 == null)
					client.sendToClient(new Message(null, Commands.defaultRes));

				else
					client.sendToClient(new Message(convertFileToSeializable(file3), Commands.reciveFile));

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case getAllReportByYearandStationId:
			ArrayList<Object> p = (ArrayList<Object>) message.getObj();
			String p_id = (String) p.get(0);
			int p_stid = (int) p.get(1);
			try {
				client.sendToClient(new Message(GasStationControllerServer.getAllReportByYearandStationId(p_id, p_stid),
						Commands.defaultRes));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case getFuelTypesByCompany:
			String companyNameByEmployee = (String) message.getObj();
			try {
				client.sendToClient(new Message(EmployeeController.getFuelTypesByCompany(companyNameByEmployee),
						Commands.defaultRes));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			break;
		case checkIfUserNameExist:
			String userName = (String) message.getObj();
			try {
				client.sendToClient(
						new Message(EmployeeController.checkIfUserNameExist(userName), Commands.defaultRes));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			break;

		case getFileToclient:
			GenericReport r = (GenericReport) message.getObj();
			File f = FileManagmentSys.getFile(FileManagmentSys.createLocation(r.getCompanyName(),
					FileManagmentSys.stationManagerReports, r.getReportType()), r.getFileName());

			try {
				client.sendToClient(new Message(convertFileToSeializable(f), Commands.reciveFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case confirmFuelMaxPriceDetails:
			ArrayList<PricingModule> PricingModuleCheckBoxlist = (ArrayList<PricingModule>) message.getObj();
			try {
				client.sendToClient(
						new Message(CompanyFuelControllerServer.checkBoxRateApproval(PricingModuleCheckBoxlist),
								Commands.defaultRes));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case rejectFuelMaxPriceDetails:
			ArrayList<PricingModule> rejectedPricingModuleCheckBoxlist = (ArrayList<PricingModule>) message.getObj();
			try {
				client.sendToClient(
						new Message(CompanyFuelControllerServer.checkBoxRateReject(rejectedPricingModuleCheckBoxlist),
								Commands.defaultRes));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case GetStationInventory:
			ArrayList<Object> array2 = (ArrayList<Object>) message.getObj();
			sendToClientObject(
					GasStationControllerServer.getStationFuelQuantity((int) array2.get(0), (String) array2.get(1)),
					client);
			break;

		case getCompanyNames:
			sendToClientObject(EmployeeController.getCompanyNames(), client);

			break;

		case addModule:
			ArrayList<Object> param = (ArrayList<Object>) message.getObj();
			sendToClientObject(
					EmployeeController.addModule((String) param.get(0), (String) param.get(1), (String) param.get(2)),
					client);
			break;

		case updateCar:
			ArrayList<Object> parame = (ArrayList<Object>) message.getObj();
			try {
				sendToClientObject(EmployeeController.updateCar((Car) parame.get(0), (String) parame.get(1)), client);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			break;

		case getCarCount:
			String idForCount = (String) message.getObj();
			try {
				sendToClientObject(EmployeeController.getCarCount(idForCount), client);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			break;

		case getAllAnaliticDataByYearAndMonth:
			ArrayList<String> parameters = (ArrayList<String>) message.getObj();
			String m = parameters.get(0);
			String y = parameters.get(1);
			String company = parameters.get(2);
			try {
				sendToClientObject(CompanyFuelControllerServer.getAllAnaliticDataByYearAndMonth(m, y, company), client);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case getAnaliticFile:
			ArrayList<String> parameters2 = (ArrayList<String>) message.getObj();
			String fileName = parameters2.get(0);
			String company2 = parameters2.get(1);
			String type = parameters2.get(2);
			try {
				client.sendToClient(new Message(
						convertFileToSeializable(CompanyFuelControllerServer.getAnaliticFile(fileName, company2, type)),
						Commands.reciveFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case getFuelAmountByFuelType:
			ArrayList<Object> values = (ArrayList<Object>) message.getObj();

			try {

				client.sendToClient(new Message(
						GasStationControllerServer.getFuelAmountByFuelType((String) values.get(0), (int) values.get(1)),
						Commands.defaultRes));

			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case getFuelTanksizebyType:
			ArrayList<Object> values1 = (ArrayList<Object>) message.getObj();

			try {

				client.sendToClient(new Message(
						GasStationControllerServer.getFuelTanksizebyType((String) values1.get(0), (int) values1.get(1)),
						Commands.defaultRes));

			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case createOrder:
			ArrayList<Object> values2 = (ArrayList<Object>) message.getObj();

			try {
				GasStationControllerServer.createOrder((int) values2.get(1), (String) values2.get(0));
				client.sendToClient(new Message(null, Commands.defaultRes));
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		default:
			System.out.println("default");
		}

	}

	/**
	 * Generic function that send to client any arrayList.
	 *
	 * @param <T> the generic type
	 * @param obj the obj
	 * @param client the client
	 */
	private <T> void sendToClientArrayList(ArrayList<T> obj, ConnectionToClient client) {
		try {
			if (obj != null) {
				if (!obj.isEmpty())
					client.sendToClient(new Message(obj, Commands.defaultRes));
				else
					client.sendToClient(new Message(obj, Commands.NoElementFound));
			}
			// there was an expetion on the server that must be handeled
			else
				client.sendToClient(new Message(null, Commands.NoElementFound));

		} catch (IOException e) {
			System.out.println("Cloud not send to client:" + client);
			e.printStackTrace();
		}
	}

	/**
	 * sends to the client single object from the arrayList.
	 *
	 * @param <T> the generic type
	 * @param obj the obj
	 * @param client the client
	 */
	private <T> void sendToClientObject(T obj, ConnectionToClient client) {
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
	 * convert File To Seializable.
	 *
	 * @param file the file
	 * @return the my file
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

	/**
	 * Server started.
	 */
	protected void serverStarted() {
		ConnectionToDB.connectToDB(schemaName, dbPassword);
		// ServerController.writeToServerConsole("Server listening for connections on
		// port " + getPort());
		// must be created first before the analitic thread
		FileManagmentSys.createSystemWorkSpace();

		//
		analticData = new AnalticData();
		thread = new Thread(analticData);
		thread.start();

		System.out.println("Server listening for connections on port " + getPort());

		//
		autoMaticPurchaseModel3Calc = new AutoMaticPurchaseModel3Calc();
		secThread = new Thread(autoMaticPurchaseModel3Calc);
		secThread.start();
	}

	/**
	 * Server stopped.
	 */
	protected void serverStopped() {
		// ServerController.writeToServerConsole("Server has stopped listening for
		// connections.");
		analticData.continueThread = false;
		try {
			thread.interrupt();
			thread.join(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			ConnectionToDB.conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Server has stopped listening for connections.");

		//
		autoMaticPurchaseModel3Calc.continueThread = false;
		try {
			secThread.interrupt();
			secThread.join(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// log out all the users
		int i = 0, size = usersOnline.size();
		UserOnline users;

		while (i < size) {
			users = usersOnline.get(0);

			try {
				if (users.userType.compareTo("customer") == 0)
					LogINController.updateUserOnlineStatus("customer", users.getUserID(), 0);

				else if (users.userType.compareTo("supplier") == 0)
					LogINController.updateUserOnlineStatus("supplier", users.getUserID(), 0);

				else
					LogINController.updateUserOnlineStatus("employee", users.getUserID(), 0);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ServerController.onlineUserTableCont(users.getUserID(), users.userType);
			i++;
		}
	}

	// just to show in the GUI
	/**
	 * String UserID ,String userType.
	 *
	 * @author iamme
	 */
	public static class UserOnline {
		
		/** The User ID. */
		String UserID;
		
		/** The user type. */
		String userType;

		/**
		 * Instantiates a new user online.
		 *
		 * @param userID the user ID
		 * @param userType the user type
		 */
		public UserOnline(String userID, String userType) {
			UserID = userID;
			this.userType = userType;
		}

		/**
		 * Gets the user ID.
		 *
		 * @return the user ID
		 */
		public String getUserID() {
			return UserID;
		}

		/**
		 * Sets the user ID.
		 *
		 * @param userID the new user ID
		 */
		public void setUserID(String userID) {
			UserID = userID;
		}

		/**
		 * Gets the user type.
		 *
		 * @return the user type
		 */
		public String getUserType() {
			return userType;
		}

		/**
		 * Sets the user type.
		 *
		 * @param userType the new user type
		 */
		public void setUserType(String userType) {
			this.userType = userType;
		}

		/**
		 * Equals.
		 *
		 * @param obj the obj
		 * @return true, if successful
		 */
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

		/**
		 * To string.
		 *
		 * @return the string
		 */
		@Override
		public String toString() {
			return "UserOnline [UserID=" + UserID + ", userType=" + userType + "]";
		}

	}
}