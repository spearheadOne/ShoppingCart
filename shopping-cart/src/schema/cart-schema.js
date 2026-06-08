import { z } from "zod";

const moneySchema = z.coerce.number().finite().nonnegative();

const cartItemResponseSchema = z.object({
    productId: z.string().min(1),
    name: z.string().min(1),
    imgUrl: z.string().min(1),
    unitPrice: moneySchema,
    quantity: z.number().int().min(1),
    lineTotal: moneySchema
}).strict();

const cartResponseSchema = z.object({
    cartId: z.string().min(1),
    items: z.array(cartItemResponseSchema),
    itemsTotal: z.number().int().nonnegative(),
    totalPrice: moneySchema
}).strict();

const cartItemAddRequestSchema = z.object({
    productId: z.string().min(1),
    quantity: z.number().int().min(1)
}).strict();

const cartItemUpdateQuantityRequestSchema = z.object({
    quantity: z.number().int().min(1)
}).strict();

export const parseCartResponse = (cartData) => cartResponseSchema.parse(cartData);
export const parseCartItemAddRequest = (request) => cartItemAddRequestSchema.parse(request);
export const parseCartItemUpdateQuantityRequest = (request) => (
    cartItemUpdateQuantityRequestSchema.parse(request)
);
