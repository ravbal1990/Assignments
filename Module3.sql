--Module 3:

--Question1: Customers with Specific Names List customer ID, name, and city for the members whose name’s the second character is ‘a’ as the last one is ‘e’. (1 point)

SELECT * FROM YRB_CUSTOMER WHERE NAME LIKE '_a%e';

--Question2: Most popular category Show the most popular category and the number of books sold in that category. (1 point) 

select a.cat,count(b.qnty) as QNTY_SOLD from yrb_book a INNER JOIN YRB_PURCHASE b ON a.title = b.title group by a.cat ORDER BY QNTY_SOLD DESC LIMIT 1;

--Question3: Customer with at most Two Membership Show customer ID, customer name, and the club name for customers who are the member of at most two clubs. Sort the result by customer id. (1 point)

select a.cid,a.name,b.club from yrb_customer a INNER JOIN yrb_member b ON a.cid = b.cid INNER JOIN (select a.cid as cid from yrb_customer a INNER JOIN yrb_member b ON a.cid = b.cid GROUP BY a.cid,a.name HAVING count(b.club) < 3) c ON b.cid = c.cid order by a.cid;

--Question4: Disfavored Categories Show the categories that customer with ID 9 has not purchased any books from those categories. (1 point)

select a.cat from yrb_category a LEFT JOIN (select a.cid,a.title,b.cat from yrb_purchase a, yrb_book b where a.title = b.title and a.year = b.year and a.cid = 9) b ON a.cat = b.cat where b.cat is NULL;

--Question5: Most Expensive and Cheapest Books List title and year of most expensive and cheapest books and the clubs that offers these books. (1 point)

select * from yrb_offer where price = (select min(price) from yrb_offer) OR price = (select max(price) from yrb_offer);

--Question6: Shipping Cost For each customer calculate the shipping cost of each of their orders. All purchases made on the same time (when) by the same customer are in one order. Sort the result based on the customer number and the purchase time (when) (1 point) 



--Question7: Different Books with the Same Title Show the title of books that have the same title but different years. (1 point)

select a.title,a.year as year1,b.year as year2 from yrb_book a, yrb_book b where a.title = b.title and a.year<b.year;

--Question8: Active and Inactive Members List customer ID, customer name, and number of purchases for each customer. First, list the active customers sorted by the number of purchases from low to high and then inactive customers sorted by their names in a descending order. (1 point)

--solution1
select a.cid,a.name,count(b.qnty) as QNTY_SOLD from yrb_customer a LEFT JOIN yrb_purchase b ON a.cid = b.cid GROUP BY a.cid,a.name ORDER BY QNTY_SOLD =0, QNTY_SOLD;

--solution2
select a.cid,a.name,count(b.qnty) as QNTY_SOLD from yrb_customer a LEFT JOIN yrb_purchase b ON a.cid = b.cid GROUP BY a.cid,a.name ORDER BY case QNTY_SOLD  when 0 then 2 else 1 end, QNTY_SOLD;

--Question9: Clubs with Offers over Average Show club name, total number of offers and the total prices for each club that its average price is over the average price of all available offers. (1 point)

select club,count(club) as "Number of Offers",avg(price) as "Total Price" from yrb_offer group by club having avg(price) > (select avg(price) from yrb_offer);

--Question10: Loyal Customers (1 point) List customer ID, name, and total purchase amount for the customers who have total purchase amount over $300. Sort the result from the highest amount of purchase to the lowest

