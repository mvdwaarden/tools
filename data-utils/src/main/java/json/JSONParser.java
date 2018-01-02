package json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;

import data.LogUtil;

/**
 * Very simple and lean JSONParser
 * 
 * @author mwa17610
 *
 */
public final class JSONParser {
	public JSONObject parse(InputStream input) {
		TokenStream tokens = new TokenInputStream(input);
		return parse(tokens, (char) 0, new JSONList(), true);
	}

	public JSONObject parse(String json) {
		TokenStream tokens = new TokenCharacterArrayStream(json);
		return parse(tokens, (char) 0, new JSONList(), true);
	}

	private JSONObject parse(TokenStream tokens, char stop, JSONObject parent, boolean root) {
		String name = "";
		JSONObject result = null;
		boolean isValue = (!root && parent instanceof JSONList);
		boolean empty = true;
		while (!tokens.eos()) {
			if (tokens.skipWhitespace()) {
				char token = tokens.getchar();
				if (token == stop)
					break;
				if (token == '"' || isCharacter(token) || isIdToken(token)) {
					String str = tokens.getString(token);
					if (isValue) {
						if (str.equals("true"))
							addToParent(parent, new JSONValue<Boolean>(true), name);
						else if (str.equals("false"))
							addToParent(parent, new JSONValue<Boolean>(false), name);
						else
							addToParent(parent, new JSONValue<String>(str), name);
						empty = false;
					} else {
						isValue = true;
						name = str;
						tokens.skipUntil(':');
					}
				} else if (token == ',') {
					isValue = (parent instanceof JSONList);
					if (empty)
						addToParent(parent, null, null);
					empty = true;
				} else if (token == '{') {
					JSONRecord record = new JSONRecord();
					if (null == result)
						result = record;
					parse(tokens, '}', record, false);
					addToParent(parent, record, name);
					empty = false;
				} else if (token == '[') {
					JSONList list = new JSONList();
					if (null == result)
						result = list;
					parse(tokens, ']', list, false);
					addToParent(parent, list, name);
					empty = false;
				} else if (isDigit(token) || token == '-') {
					tokens.ungetchar();
					JSONValue<?> number = tokens.getNumber();
					addToParent(parent, number, name);
					empty = false;
				}
			}
		}

		return result;
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isCharacter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'B');
	}

	private boolean isIdToken(char c) {
		return c == '$' || c == '_';
	}

	private static void addToParent(JSONObject parent, JSONObject object, String name) {
		if (parent instanceof JSONRecord)
			((JSONRecord) parent).getData().put(name, object);
		else if (parent instanceof JSONList)
			((JSONList) parent).getData().add(object);
	}

	abstract class TokenStream {
		abstract public char getchar();

		abstract public void ungetchar();

		abstract public boolean eos();

		private boolean skipWhitespace() {
			while (!eos()) {
				char token = getchar();
				if (token != '\t' && token != '\n' && token != '\r' && token != ' ') {
					ungetchar();
					break;
				}
			}
			return !eos();
		}

		private boolean skipUntil(char c) {
			while (!eos()) {
				char token = getchar();
				if (token == c) {
					ungetchar();
					break;
				}
			}
			return !eos();
		}

		public char[] getchars(int size) {
			char[] result = null;
			char[] tmp = new char[size];
			int actualSize = 0;
			while (actualSize < size && !eos())
				tmp[actualSize++] = getchar();
			result = new char[actualSize];
			System.arraycopy(tmp, 0, result, 0, result.length);

			return result;
		}

		private JSONValue<?> getNumber() {
			StringBuilder result = new StringBuilder();
			boolean isInt = true;
			while (!eos()) {
				char token = getchar();

				if (isDigit(token) || token == '.' || token == 'E' || token == 'e') {
					isInt = (isInt && token != '.');
					result.append(token);
				} else {
					ungetchar();
					break;
				}
			}
			if (null == result || result.toString().isEmpty()) {
				getchar();
				return new JSONValue<Integer>(0);
			} else if (isInt) {
				if (result.length() > 18)
					return new JSONValue<BigInteger>(new BigInteger(result.toString()));
				else
					return new JSONValue<Long>(Long.parseLong(result.toString()));
			} else
				return new JSONValue<Double>(Double.parseDouble(result.toString()));
		}

		private String getString(char starttoken) {
			StringBuilder result = new StringBuilder();
			if (starttoken != '"')
				result.append(starttoken);
			while (!eos()) {
				char token = getchar();
				if (token == '"' && starttoken == '"') {
					return result.toString();
				} else if (starttoken != '"' && token == ':') {
					return result.toString().trim();
				} else if (starttoken != '"' && (token == ',' || token == '}' || token == ']')) {
					ungetchar();
					return result.toString().trim();
				} else if (token == '\\') {
					token = getchar();
					if (token == '/' || token == '\\') {
						result.append(token);
					} else if (token == 'u') {
						char[] code = getchars(4);
						result.append("\\u");
						result.append(code);
					} else if (token == 't' || token == 'r' || token == 'n' || token == 'f' || token == '"'
							|| token == 'b') {
						result.append("\\" + token);
					}
				} else {
					result.append(token);
				}
			}

			return result.toString();
		}
	}

	class TokenInputStream extends TokenStream {
		private CyclicBuffer<Character> buffer = new CyclicBuffer<Character>(new Character[10]);
		private BufferedReader reader;
		private boolean eos;

		public TokenInputStream(InputStream is) {
			this.reader = new BufferedReader(new InputStreamReader(is));
		}

		@Override
		public char getchar() {
			char result = 0;
			try {
				if (!buffer.hasItems()) {
					int c = reader.read();
					if (c < 0) {
						eos = true;
						buffer.put((char) 0);
					} else {
						buffer.put((char) c);
					}
				}
				result = buffer.get();
			} catch (IOException e) {
				LogUtil.getInstance().info("problem getting character from inputstream, eos?", e);
				eos = true;
			}

			return result;
		}

		@Override
		public void ungetchar() {
			buffer.unget();
		}

		@Override
		public boolean eos() {
			return eos;
		}
	}

	class TokenCharacterArrayStream extends TokenStream {
		public char[] tokens;
		public int idx;

		public TokenCharacterArrayStream(String json) {
			this.tokens = json.toCharArray();
		}

		@Override
		public char getchar() {
			if (idx % 10000000 == 0)
				System.out.println(idx);
			if (!eos())
				return tokens[idx++];
			else
				return '0';
		}

		@Override
		public void ungetchar() {
			--idx;
		}

		@Override
		public boolean eos() {
			return idx >= tokens.length;
		}

	}

	class CyclicBuffer<T> {
		private T[] buffer;
		private int idxGet;
		private int idxPut;

		public CyclicBuffer(T[] buffer) {
			this.buffer = buffer;
		}

		public void put(T item) {
			this.buffer[idxPut++] = item;
			if (idxPut >= buffer.length)
				idxPut = 0;
		}

		public T get() {
			T result = this.buffer[idxGet++];
			if (idxGet >= buffer.length)
				idxGet = 0;

			return result;
		}

		public void unget() {
			--idxGet;
			if (idxGet < 0)
				idxGet = buffer.length - 1;
		}

		public boolean hasItems() {
			return idxGet != idxPut;
		}
	}
}