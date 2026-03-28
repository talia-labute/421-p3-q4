CREATE TRIGGER TRIG_ORDERITEM_ADD
AFTER INSERT ON OrderItem
REFERENCING NEW AS N
FOR EACH ROW
BEGIN ATOMIC
    UPDATE Orders
    SET totalAmount = totalAmount + (N.quantity * N.priceAtPurchase)
    WHERE orderId = N.orderId;
END