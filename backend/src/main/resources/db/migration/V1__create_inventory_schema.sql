-- Catalog of dresses offered by the boutique.
create table dress
(
    id         uuid primary key,
    name       varchar(200)   not null,
    image_url  varchar(1000),
    price      numeric(12, 2) not null check (price >= 0),
    version    bigint         not null default 0,
    created_at timestamptz    not null default now(),
    updated_at timestamptz    not null default now()
);

-- Stock per color variant. The check constraint is the last line of
-- defense against overselling, even if application logic fails.
create table stock_item
(
    id       uuid primary key,
    dress_id uuid         not null references dress (id) on delete cascade,
    color    varchar(100) not null,
    quantity integer      not null check (quantity >= 0),
    constraint uq_stock_item_dress_color unique (dress_id, color)
);

-- Orders keep a snapshot of the dress (name, image, price) so that
-- history stays correct after the catalog changes or a dress is removed.
create table customer_order
(
    id                     uuid primary key,
    dress_id               uuid           references dress (id) on delete set null,
    dress_name             varchar(200)   not null,
    image_url              varchar(1000),
    unit_price             numeric(12, 2) not null check (unit_price >= 0),
    color                  varchar(100)   not null,
    quantity               integer        not null check (quantity > 0),
    payment_screenshot_url varchar(1000),
    created_at             timestamptz    not null default now()
);

create index idx_customer_order_created_at on customer_order (created_at desc);
create index idx_customer_order_screenshot on customer_order (payment_screenshot_url);
