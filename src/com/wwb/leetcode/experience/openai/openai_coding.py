# 题目是给一个tree，里面的每一个node都是一个machine，只有parent和child之间可以通讯。通讯是靠sendAsyncMessage()和receiveMessage()， 
# 那个sendmessage不需要自己实现，属于提供的一个interface，可以直接用。现在需要你设计一个方法，去统计一共有多少machine。这是第一问，相对比较容易一些，理解他想让你干嘛之后，就实现这个receiveMessage的method就好，基本的逻辑就是，判断Message，如果是count，就发同样消息给自己的child。如果是response的话，就把count number记录下来，等把所有child都记录下来之后，做一个sum，然后返回给自己的parent。里面有一些case handle好就没问题 （比如当是root或者leaf的情况）
# 题目是给一个n叉树代表一个cluster的nodes。有一个root。每个node只能跟自己的parent或者child通信，root除外，
# root能收到外部来的message，这题要求发给root一个信息，让其统计树的node数量并打印出来。要求实现一个Node class，
# 里面的API有receiveMessage(from_node_id, message)和sendMessage(to_node_id, message)。sendMessage不需要implement，可以call到它。
# 默认node一旦被发送message，就会自动执行receiveMessage（）。最开始root会收到receiveMessage(null, message)。follow up：
# 如果系统中会有network failure，就会有retry，那如何保证不重复计算？也就是怎样设计以保证idempotency，答曰node里加一个set。

# 第二问是一样的题，需要扩展feature，这次不是数数了，需要你返回整个树的topology。其实第二问跟第一问差不太多，基本就是定义两个新的Message就行，之前是count，countResponse。那现在就是topology和topologyResponse，当然Response里面也应该把你实际需要传回的数据一起带回来。


# sendMessage(nodeId, string)
# sendAsyncMessage(fromNodeId, string)

class Node:
	def __init__(self, node_id: str, children: List[str], parent: str):
		self.node_id = node_id
		self.children = children
		self.parent = parent
		self.total_count = 0
		self.records = []

	def sendAsyncMessage(node_id, string):
		pass

	def receiveMessage(fromNodeId, string):
		# is root
		if not fromNodeId:
			if not self.children:
				print('1')
			else:
				for child in self.children:
					sendAsyncMessage(child, '0')				

		# get message from parent
		if fromNodeId == self.parent:
			if not self.children:
				sendAsyncMessage(self.parent, '1')

			for child in self.children:
				sendAsyncMessage(child, '0')

		else if fromNodeId in self.children:
			if fromNodeId in self.records:
				return

			sub_tree_count = int(string)
			self.total_count += sub_tree_count
			self.records.append(fromNodeId)

			if len(self.records) == len(self.children):
				sendAsyncMessage(self.parent, str(self.total_count))
				
				self.total_count = 0
				self.records = []


# Implement two classes, Node and Function, to create a custom iterator. The Node class can be initialized with either a string or a list of Node
# instances. The to_str method of the Node class should represent the node as a string in a specific format. The Function class should have two
# attributes: input parameters and output type, both represented by Node instances. The to_str method for the Function class should also
# represent the function in a specific format. Additionally, implement a method get_return_type that takes a Function object and invocation
# arguments, and returns the output type as a Node instance. This method should check if the invocation arguments match the function's
# parameters, considering generic types and the possibility of nested Node instances.

from typing import Union, List


class Node:
	def __init__(self, node_type: Union[str, List['Node']]):
		self.type_list = ['str', 'float', 'int']

		if isinstance(node_type, str):
			self.base = node_type
			self.children = []
		else:
			self.base = None
			self.children = node_type
	def get_content(self) -> Union[str, List['Node']]:
		if self.base:
			return self.base

		return self.children

	def is_base_generic_type(self):
		return self.base and self.base not in self.type_list

	def is_generic_type(self):
		if self.is_base_generic_type():
			return True

		return any([child.is_generic_type() for child in self.children])

	def clone(self):
		if self.base:
			return Node(self.base)
		return Node([child.clone() for child in self.children])


	def __str__(self) -> str:
		if self.base:
			return self.base

		node_types = []
		for child in self.children:
			node_types.append(str(child))
		
		return f'[{','.join(node_types)}]'

	def __eq__(self, other) -> bool:
		if not isinstance(other, Node):
			return False
		return str(other) == str(self)


class Function:
	def __init__(self, param: List[Node], output: Node):
		self.parameters = param
		self.output_type = output

	def __str__(self) -> str:
		param_str = ','.join([str(param) for param in self.parameters])
		output_str = str(self.output_type)

		return f'({param_str}) -> {output_str}'

def binding(func_param: Node, param: Node, binding_map: dict):
	# func param is generic type
		# not binded
		# already binded, and type matched
		# already binded, and type mismatched
	# func param is not generic type
		# both type match
		# type mismatch
			# nested, has generic type inside
	if func_param.is_generic_type() and func_param.base:
		if func_param.base in binding_map and binding_map[func_param.base] != param:
			raise Exception(f'invocation argument type mismatched on {func_param} and {param}')
		if func_param.base not in binding_map:
			binding_map[func_param.base] = param
	elif func_param == param:
		return
	elif not func_param.base and not param.base:
		for sub_func_node, sub_param_node in zip(func_param.children, param.children):
			binding(sub_func_node, sub_param_node, binding_map)
	else:
		raise Exception(f'mismatch paramter on {func_param} and {param}')


def replace_invocation_arguments(node: Node, binding_map: dict) -> Node: 
	if not node.is_generic_type():
		return node.clone()

	if not node.children:
		cloned = binding_map[node.base].clone()

		return Node(cloned.get_content())

	return Node([replace_invocation_arguments(child, binding_map) for child in node.children])


def get_return_type(parameters: List[Node], function: Function) -> Node:
	if len(parameters) != len(function.parameters):
		raise Exception("Illegal Arguments")

	binding_map = {}

	for func_node, param_node in zip(function.parameters, parameters):
			binding(func_node, param_node, binding_map)

	if not function.output_type.is_generic_type():
		return function.output_type

	return replace_invocation_arguments(function.output_type, binding_map)



node1 = Node('T')
node2 = Node('float')
node3 = Node('T')
node4 = Node([node1, node2])
node5 = Node([node4, node3])


print(node5)

func = Function([node5, Node('S')], Node([Node('S'), Node('T')]))

print(func)

node11 = Node('str')
node22 = Node('float')
node33 = Node('str')
node44 = Node([node11, node22])
node55 = Node([node44, node33])


node = get_return_type([node55, Node([Node('float'), Node('int')])], func)
print(node)




# 要求实现cd(current_dir, new dir), 返回最终的path, 比如：
# cd(/foo/bar, baz) = /foo/bar/baz
# cd(/foo/../, ./baz) = /baz
# cd(/, foo/bar/../../baz) = /baz
# cd(/, ..) = Null
# 第二问可不可以加上对～符号也就是home directory的支持
# 完成以后难度加大，第三个参数是soft link的dictionary，比如：
# cd(/foo/bar, baz, {/foo/bar: /abc}) = /abc/baz
# cd(/foo/bar, baz, {/foo/bar: /abc, /abc: /bcd, /bcd/baz: /xyz}) = /xyz
# dictionary 里有可能有短匹配和长匹配，应该先匹配长的(more specific), 比如：
# cd(/foo/bar, baz, {/foo/bar: /abc, /foo/bar/baz: /xyz}) = /xyz
# 要判断dictionary里是否有循环

'USE trie tree traverse through the symlink build the tree'
'each walk through will the O(n) n as length of input path'



from typing import Union, List

def cd(current_dir: str, new_dir: str) -> str:
	return simplify_path(current_dir + '/' + new_dir)

def simplify_path(path: str) -> str:
	if not path:
		return '/'

	path_stack = []
	path_tokens = path.split('/')

	for token in path_tokens:
		if token == '..' and path_stack:
			path_stack.pop()
		elif not token or token == '.':
			continue
		elif token == '..' and not path_stack:
			return 'NULL'
		else:
			path_stack.append(token)

	return '/' + '/'.join(path_stack)


class TrieNode:
	def __init__(self, path: str):
		self.path = path
		self.link_path = None
		self.children = {}

def build_trie(symlinks: dict) -> TrieNode:
	root = TrieNode('')

	for symlink_path, link in symlinks.items():
		insert_path(symlink_path, root, link)
	return root


def insert_path(path: str, root: TrieNode, symlink: str):
	path_tokens = path.split('/')
	mover = root

	for token in path_tokens:
		if token not in mover.children:
			mover.children[token] = TrieNode(token)
		
		mover = mover.children[token]

	mover.link_path = symlink.split('/')

def convert_path(path_tokens: List[str], trie_root: TrieNode) -> List[str]: 
	mover = trie_root

	cur_replaced = []
	cur_index = -1

	for index, token in enumerate(path_tokens):
		if token not in mover.children:
			break

		mover = mover.children[token]
		if mover.link_path:
			# print(f"matched - {index} - {token} - {mover.link_path}")
			cur_replaced = mover.link_path
			cur_index = index + 1

	if not cur_replaced:
		return []

	if cur_index == len(path_tokens):
		suffix = []
	else:
		suffix = path_tokens[cur_index:]


	return cur_replaced + suffix



def cd_symlinks(current_dir: str, new_dir: str, symlinks: dict) -> str:
	path = simplify_path(current_dir + '/' + new_dir)

	if path == 'NULL':
		return path

	trie_root = build_trie(symlinks)
	result_path = path.split('/')
	visited = set('/'.join(result_path))

	while result_path:
		converted = convert_path(result_path, trie_root)

		if '/'.join(converted) in visited:
			raise Exception("loop detected in symlink")
		if not converted:
			break

		visited.add('/'.join(result_path))
		result_path = converted

	return '/'.join(result_path)

print(cd_symlinks("/foo/bar", "baz", {"/foo/bar": "/abc"}))  # /abc/baz
print(cd_symlinks("/foo/bar", "baz", {
    "/foo/bar": "/abc",
    "/abc": "/bcd",
    "/bcd/baz": "/xyz"
}))  # /xyz
print(cd_symlinks("/foo/bar", "baz", {
    "/foo/bar": "/abc",
    "/foo/bar/baz": "/xyz"
}))  # /xyz
# print(cd_symlinks("/a", "b", {
#     "/a": "/b",
#     "/b": "/a"
# }))  # None (loop)

# 给一个Spreadsheet API，要求实现getCell和setCell，
# 其中setCell可以依赖于其他Cell或者是提供独立的value，比如setCell("A", "B+C")，setCell("A", 100)
# Excel sheets, getCell(), setCell(), handle cycles in sheets. Expected to write tests. First-part is OK to be implemented with sub-optimal getCell() implementation where the value is computed on the fly. Second part, the setCell() is supposed to update the values of impacted cells so that getCell is O(1)。You have to implement two functions getCell() and setCell(). Handle cycles in sheet. 考察的就是recursion
# Some tests are:
#     spreadsheet = Spreadsheet()
#     spreadsheet.setCell('A1', '1')
#     spreadsheet.setCell('A2', '2')
#     spreadsheet.setCell('A3', '=A1+A2')
#     spreadsheet.setCell('A4', '=A3+A2')
#     spreadsheet.setCell('A5', '=A3+A4')
#     spreadsheet.setCell('B1', '=A1+A2+A3+A4+A5')
# some other tests are:
# 这个SPREAD SHEET
# Cell A = Cell (6, NULL, NULL)
# Cell B = Cell (7, NULL, NULL)
# Cell C = Cell (13, A, B)
# print getCell(C) => 13
# A+B = 6+7 = 13
# 但之后如果我们把A改了，
# C因为是A和B的和，
# 数字也要自动更新，
# update Cell A = Cell (2, NULL, NULL)
# print getCell(C) => 9
# A+B = 2+7 = 9

# 一个cell可以是int或者一个formula（A1 + B1). 简单的dfs就可以，
# 然后问怎么更快优化，更高效的搜索, 基本上面试官想看如何针对多个request之间的优化。

from typing import List
from collections import defaultdict


class Cell:
	def __init__(self, name: str, value: int):
		self.name = name
		self.value = value
		self.sources = defaultdict(int) # key-cell, value-indegree of each cell in source -> could be (A1 + A2) + (A1 + A3) = A1*2 + A2 + A3
		self.effected = set() # list of Cells

	def update_value_by_cell(self, cell: 'Cell', old_value: int, new_value: int):
		if cell not in self.sources:
			raise Exception(f'illegal update value for {self.name} by cell {cell.name}')

		self.value += new_value * self.sources[cell] - old_value * self.sources[cell]

	def update_source(self, cell: 'Cell', update_count: int, update_from_cell: 'Cell') -> int:
		self.sources[cell] += update_count * self.sources[update_from_cell]
		if self.sources[cell] == 0:
			del self.sources[cell]
		if self.sources[cell] < 0:
			self.sources[cell] -= update_count # revert
			raise Exception('illegal update source')

		return self.sources[cell]


	
	def __str__(self):
		str_value = f'name-{self.name} | value-{self.value} \n \
|= sources-{[(source.name, count) for source, count in self.sources.items()]} \n \
|= effected-{[effect.name for effect in self.effected]} \n' 
		return str_value

	def __repr__(self):
		return self.__str__()

class Sheet:
	def __init__(self):
		self.cells = {}

	# time complexity - 
	# when update existing cell with value only - O(downstream nodes)
	# when update existing cell with sources - O(all transitive source nodes + downstream nodes)
	# when add new cell with sources - O(source nodes)

	def setCell(self, name: str, value: int, sources: List[str] = []) -> int:
		all_sources = defaultdict(int)
		# either value of source must be set
		# thus if source is there, value is always 0
		new_value = value

		for source_name in sources:
			source = self.cells[source_name]

			all_sources[source] += 1
			for sub_source, count in source.sources.items():
				all_sources[sub_source] += count
			
			new_value += source.value

		if name not in self.cells:
			cell = Cell(name, value)
			for source in all_sources.keys():
				source.effected.add(cell)

			cell.value = new_value
			cell.sources = all_sources
			self.cells[cell.name] = cell
		else:
			# update existing cell
			# 1. check if there is loop
			# 2. update source if needed
			# 2.5 update discarded source if needed
			# 3. update self value
			# 4. update effected

			cell = self.cells[name]
			
			if cell in all_sources:
				raise Exception('invalid operation, loop detected')

			source_diffs = defaultdict(int)
			for new_source, count in all_sources.items():
				source_diffs[new_source] += count
			for old_source, count in cell.sources.items():
				source_diffs[old_source] -= count
				if source_diffs[old_source] <= 0:
					old_source.effected.remove(cell)

			for source in all_sources.keys():
				source.effected.add(cell)
				source.effected.update(cell.effected)

			for effect_cell in cell.effected:
				for source_diff, count in  .items():
					if not effect_cell.update_source(source_diff, count, cell):
						source_diff.effected.remove(effect_cell)

				effect_cell.update_value_by_cell(cell, cell.value, new_value)

			cell.value = new_value
			cell.sources = all_sources

		return cell.value

	def getCell(self, name: str) -> int:
		return self.cells[name].value



sheet = Sheet()

print('A1', sheet.setCell('A1', 7))
print('A2', sheet.setCell('A2', 6))
print('A3', sheet.setCell('A3', 0, ['A1', 'A2']))
print('A5', sheet.setCell('A5', 9))
print('A4', sheet.setCell('A4', 0, ['A3', 'A5']))
print('A9', sheet.setCell('A9', 0, ['A1']))
print('A3', sheet.setCell('A3', 0, ['A1', 'A2', 'A9']))
print('A10', sheet.setCell('A10', 0, ['A3', 'A9']))
print('A11', sheet.setCell('A11', 0, ['A4', 'A10']))
print(sheet.cells)
print('A3', sheet.setCell('A3', 0, ['A2']))
print('A10', sheet.getCell('A10'))
print('A4', sheet.getCell('A4'))
print('A11', sheet.getCell('A11'))
print(sheet.cells)
print('A1', sheet.setCell('A1', 8))
print(sheet.cells)


# sql：Memory db, insert, query, where filter, order by。select(table_name, where=None, order_by=None) ，
# 多个where的情况下只支持and。Query with where condition。Query with where condition on multiple columns。
# Query with where condition and order by one column。Query with where condition and order by multiple columns。
# 这里注意几个问题需要用同一个api所以要处理一下input param，确保backward compatibility。

# from typing import 

class InMemDB:
	def __init__(self):
		self.tables = {} # key-table name, value-table rows
		self.schemas = {} # key-table name

	def create_table(self, table_name, columns):
		self.schema[table_name] = schema
		self.tables[table_name] = []

	def insert(self, table, row): 
		table = self.tables[table]

		table.append(row.copy())

	def select(self, table, where=None, order_by=None):
		rows = self.tables[table]

		if where:
			rows = list(filter(rows, where))

		if order_by:
			rows = sorted(rows, key=lambda x: x[order_by])

		return rows


db = MemoryDB()
db.create_table("users", ["id", "name", "age"])

db.insert("users", {"id": 1, "name": "Alice", "age": 30})
db.insert("users", {"id": 2, "name": "Bob", "age": 25})
db.insert("users", {"id": 3, "name": "Charlie", "age": 35})

# Query with where and order_by
results = db.select("users", where=lambda row: row["age"] > 28, order_by="name")
for row in results:
    print(row)


# 这个题有几个地方要注意1. Input可以是out of order的, 可能会在加timestamp=20, 减timestamp=30 之后 check balance timestamp =10  2. 一个timestamp只会收到一次减 3. 如果这个时间的balance不够，要return None 4. Substract的时候要根据expiration time 从最早expire的timestamp开始 

# 充值，给一个时间段 [A, B]充值 X ，充值只在这个时间段之间有效。可以充值多次，时间段可overlap 花钱，在时间T花X块 查余额，给一个时间T，问这个时间点有多少余额.

# GPU Credit
# Implement a GPU credit calculator.
# ```python
# class GPUCredit:
#         def addCredit(creditID: str, amount: int, timestamp: int, expiration: int) -> None：
#     # A credit is an offering of GPU balance that expires after some expiration-time. The credit can be used only during [timestamp, timestamp + expiration]. A credit can be repeatedly used until expiration.
       
#         def getBalance(creditId, timestamp: int) -> int | None: # return the balance remaining on the account at the timestamp, return None if there are no credit left. Note, balance cannot be negative. See edge case below.
         
#         def useCredit(creditId, timestamp: int, amount: int) -> None
# ```
# Example 1:
# ```python
# gpuCredit = GPUCredit()
# gpuCredit.addCredit('microsoft', 10, 10, 30)
# gpuCredit.getBalance(0) # returns None
# gpuCredit.getBalance(10) # returns 10
# gpuCredit.getBalance(40) # returns 10
# gpuCredit.getBalance(41) # returns None
# ```
# Example 2:
# ```python
# gpuCredit = GPUCredit()
# gpuCredit.addCredit('amazon', 40, 10, 50)
# gpuCredit.useCredit(30, 30)
# gpuCredit.getBalance(40) # returns 10
# gpuCredit.addCredit('google', 20, 60, 10)
# gpuCredit.getBalance(60) # returns 30
# gpuCredit.getBalance(61) # returns 20
# gpuCredit.getBalance(70) # returns 20
# gpuCredit.getBalance(71) # returns None
# ```
# Edge Case:
# ```python
# gpuCredit = GPUCredit()
# gpuCredit.addCoupon('openai', 10, 10, 30)
# gpuCredit.useCoupon(10, 100000000)
# gpuCredit.getBalance(10) # returns None
# gpuCredit.useCoupon('openai', 10, 20, 10)
# gpuCredit.getBalance(20) # returns 10
# ```

from collections import defaultdict

class CreditToken:
	def __init__(self, start_time, expiration, amount):
		self.start_time = start_time
		self.end_time = start_time + expiration
		self.amount = amount

	def __repr__(self):
		return f'{self.start_time} - {self.end_time} - {self.amount}'

class GPUCredit:
	def __init__(self):
		self.user_credits = defaultdict(list)
		self.user_costs = defaultdict(list)

	def add_credit(self, credit_id, start_time, expiration, amount):
		self.user_credits[credit_id].append(CreditToken(start_time, expiration, amount))
		self.user_credits[credit_id].sort(key=lambda x: x.start_time)

	def get_balance(self, credit_id, time):
		if credit_id not in self.user_credits:
			return None

		credits = self.user_credits[credit_id]
		costs = self.user_costs[credit_id]

		balance = 0
		out_of_bound = True

		for token in credits:
			if token.end_time < time:
				continue
			if token.start_time > time:
				break

			balance += token.amount
			out_of_bound = False

		for cost in costs:
			if cost[0] > time:
				break
			balance -= cost[1]

		if out_of_bound:
			return None
		return balance

	def use_credit(credit_id, amount, time):
		self.user_costs[credit_id].append((time, amount))
		self.user_costs[credit_id].sorted(key=lambda x: x[0])

	
# [1, 30, 40]
# [20, 50, 60]

# => [1, 20, 40] [20, 30, 100] [30, 50, 60]



credit = GPUCredit()
credit.add_credit("me", 1, 20, 40)
credit.add_credit("me", 21, 30, 60)

print(credit.get_balance('me', 10))
print(credit.get_balance('me', 21))
print(credit.get_balance('me', 31))
print(credit.get_balance('me', 61))




























