package com.github.javlock.lstr.data;

import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.github.javlock.lstr.AppHeader;
import com.github.javlock.lstr.utils.CryptUtils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "messages")
public class Message implements Serializable {

	private static final long serialVersionUID = 313854771560527060L;

	private @Getter @DatabaseField(id = true) String id = UUID.randomUUID().toString();

	private @Getter long timeCreated = System.currentTimeMillis() / 1000;

	private @Getter @Setter @DatabaseField String encMsg;
	private transient @Getter @Setter @DatabaseField String rawMsg;
	private @Getter @Setter @DatabaseField String from;
	private @Getter @Setter @DatabaseField String to;

	public boolean decryptFor(AppInfo contact)
			throws SQLException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {

		EncryptKey encryptKey = AppHeader.app.dataBase.getKeyFor(contact, AppHeader.getConfig().getTorDomain());
		String pass = encryptKey.getKey();
		rawMsg = CryptUtils.decryptFor(pass, encMsg);
		return true;
	}

	public boolean encryptFor(AppInfo contact)
			throws SQLException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		EncryptKey encryptKey = AppHeader.app.dataBase.getKeyFor(contact, AppHeader.getConfig().getTorDomain());

		String saltRaw = encryptKey.getSalt();
		String pass = encryptKey.getKey();

		encMsg = CryptUtils.encryptFor(saltRaw, pass, rawMsg);
		rawMsg = CryptUtils.decryptFor(pass, encMsg);
		final String OUTPUTFORMAT = "%-30s:%s";

		System.out.println(String.format(OUTPUTFORMAT, "Input (plain text)", rawMsg));
		System.out.println(String.format(OUTPUTFORMAT, "Encrypted (base64) ", encMsg));
		System.out.println(String.format(OUTPUTFORMAT, "Input (base64)", encMsg));
		System.out.println(String.format(OUTPUTFORMAT, "Decrypted (plain text)", rawMsg));
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Message)) {
			return false;
		}
		Message other = (Message) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public void repare(Dao<Message, String> messageDao, AppInfo myInfo, AppInfo domainInfo)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, SQLException {

		boolean needUpdate = false;
		if (getRawMsg() == null && getEncMsg() != null && decryptFor(myInfo)) {
			needUpdate = true;
		}
		if (getRawMsg() != null && getEncMsg() == null && encryptFor(domainInfo)) {
			needUpdate = true;
		}
		if (needUpdate) {
			messageDao.update(this);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Message [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		builder.append("timeCreated=");
		builder.append(timeCreated);
		builder.append(", ");
		if (encMsg != null) {
			builder.append("encMsg=");
			builder.append(encMsg);
			builder.append(", ");
		}
		if (rawMsg != null) {
			builder.append("rawMsg=");
			builder.append(rawMsg);
		}
		builder.append("]");
		return builder.toString();
	}// no send

}