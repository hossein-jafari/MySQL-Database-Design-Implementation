/* Create table for branches */
Create table Branch (BranchID int (2) not null unique, 
Address varchar (30) not null, 
County varchar (10) not null, 
primary key (BranchID));

/* Create table for phone numbers */
Create table BranchPhone (PhoneNumber char (9) not null, 
BranchID int (2) not null, 
primary key (PhoneNumber), 
foreign key (BranchID) references Branch (BranchID) on delete cascade on update cascade);

/* Create table for staff */
Create table Staff (StaffNumber int (4) not null auto_increment, 
FirstName varchar (15) not null, 
Surname varchar (20) not null, 
BranchID int (2) not null, 
HomeAddress varchar (30) not null, 
PhoneNumber char (11) not null, 
DateOfBirth date not null, 
DateJoined date not null, 
JobTitle varchar (15) not null, 
Salary decimal (6,2), 
primary key (StaffNumber), 
foreign key (BranchID) references Branch (BranchID) on delete cascade on update cascade);

/* Create table for car classes */
Create table CarClass (ClassName char (1) not null unique, 
entalPrice int (2), 
primary key (ClassName));

/* Create table for car models */
Create table CarModel (ModelName varchar (15) not null unique, 
ClassName char (1) not null, 
Manufacturer varchar (15) not null, 
EngineSize varchar (20) not null, 
Capacity int (2) not null, 
primary key (ModelName), 
foreign key (ClassName) references CarClass (ClassName) on delete cascade on update cascade);

/* Create table for cars */
Create table Car (CarRegNumber int (4) not null auto_increment, 
Ownership varchar (10) not null, 
BranchID int (2) not null, 
ModelName varchar (15) not null, 
Color varchar (10) not null, 
CurrentMileage int (6) not null, 
Status varchar (9) not null, 
DateNCTDue date not null, 
DateAcquired date not null, 
primary key (CarRegNumber), 
foreign key (BranchID) references Branch (BranchID) on delete cascade on update cascade, 
foreign key (ModelName) references CarModel (ModelName) on delete cascade on update cascade);

/* Create table for customers */
Create table Customer (CustomerNumber int not null auto_increment, 
FirstName varchar (15) not null, 
Surname varchar (20) not null, 
HomeAddress varchar (3) not null, 
PhoneNumber char (11) not null, 
DateOfBirth date not null, 
DrivingLicenseNumber int not null, 
primary key (CustomerNumber));

/* Create table for hiring contracts */ 
Create table Hire (HireNumber int not null auto_increment, 
BranchID int (2) not null, 
CustomerNumber int not null, 
CarRegNumber int (4) not null, 
StaffNumber int (4) not null, 
Duration int (3) not null, 
StartDate date not null, 
TerminationDate date not null, 
MileageBefore int (6) not null, 
MileageAfter int (6) not null, 
primary key (HireNumber), 
foreign key (BranchID) references Branch (BranchID) on delete cascade on update cascade, 
foreign key (CustomerNumber) references Customer (CustomerNumber) on delete cascade on update cascade, 
foreign key (CarRegNumber) references Car (CarRegNumber) on delete cascade on update cascade, 
foreign key (StaffNumber) references Staff (StaffNumber) on delete cascade on update cascade);

/* Find all available class B cars in a particular branch and show their details */
Select * from Car where CarRegNumber in (select CarRegNumber from Car, 
CarModel where Car.ModelName= CarModel.ModelName and CarModel.ClassName= “B” and Car.BranchId=2);

/* Update a customer’s phone number */
Update Customer set PhoneNumber=”01-334522” where FirstName= “John”;

/* Show customers whose surname starts with “Co” */
Select * from Customer where Surname like “Co%”;

/* Show details of all customers that rented a particular car sorted by rental date with the latest being shown first */
Select * from Customer where CustomerNumber in 
(select CustomerNumber from Hire where CarRegNumber=13 order by StartDate desc);

/* Show class, cost and model of all red cars */
Select * from Car, CarModel, CarClass where 
Car.ModelName= CarModel.ModelName and CarModel.ClassName= CarClass.ClassName and Car.Color= “Red”;

/* Show details of car models that were rented more than 4 times. Remove duplicates */
Select * from CarModel where ModelName in 
(select ModelName from Car where CarRegNumber in 
(select CarRegNumber from Hire group by CarRegNumber having count(*)>4));

/* Increase all prices by 10% */
Update CarClass set RentalPrice=RentalPrice * 1.10;

/* Show names of manufacturers that have more than 6 broken cars */
Select Manufacturer from CarModel where ModelName in 
(select ModelName from Car where Status= “Broken” and Status in 
(select Status from Car group by Status having count(*)>6));

/* Which car model is the most popular among customers */
Select ModelName from Car where CarRegNumber in 
(Select max(count1) as mostpopular from 
(Select CarRegNumber, count(CarRegNumber) as count1 from Hire group by CarRegNumber) mostpopular);

/* Show number of purchase and average of distance each customer has made */
Select CustomerNumber, count(CustomerNumber), avg(Duration) from Hire group by CustomerNumber;

/* Show car models which have been hired between 2018 and 2019 */
 Select ModelName from Car where CarRegNumber in 
 (select CarRegNumber from Hire where StartDate> “2018/01/01” and StartDate<”2019/01/01”);