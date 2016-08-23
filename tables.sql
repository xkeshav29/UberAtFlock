CREATE TABLE installs(
userid varchar(100) NOT NULL,
usertoken varchar(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS `oauth_token` (
  `user_id` varchar(50) NOT NULL,
  `display_name` varchar(50) DEFAULT NULL,
  `access_token` varchar(200) DEFAULT NULL,
  `refresh_token` varchar(200) DEFAULT NULL,
  `expiry` timestamp NULL,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  INDEX(`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `oauth_csrf_token_cache` (
  `user_id` varchar(50) NOT NULL,
  `token` varchar(32) NOT NULL,
  `expiry` timestamp NOT NULL,
  PRIMARY KEY (`user_id`)
)
