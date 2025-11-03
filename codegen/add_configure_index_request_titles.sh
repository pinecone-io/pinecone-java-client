#!/bin/bash

set -eu -o pipefail

# Simple script to add titles to nested objects in ConfigureIndexRequest schema
# Adds titles to the serverless and pod nested inline objects

if [ $# -lt 1 ]; then
	echo "Usage: $0 <yaml_file>"
	exit 1
fi

file="$1"

if [ ! -f "$file" ]; then
	echo "Error: File does not exist: $file"
	exit 1
fi

echo "Processing: $file"

# Read file into array
lines=()
while IFS= read -r line || [ -n "$line" ]; do
	lines+=("$line")
done < "$file"
total_lines=${#lines[@]}

in_configure_index_request=false
changed=false
i=0

while [ $i -lt $total_lines ]; do
	line="${lines[$i]}"
	
	# Detect ConfigureIndexRequest schema
	if echo "$line" | grep -qE '^[[:space:]]*ConfigureIndexRequest:'; then
		in_configure_index_request=true
	fi
	
	# Check if we've left ConfigureIndexRequest (hit another top-level schema)
	if [ "$in_configure_index_request" = true ]; then
		if echo "$line" | grep -qE '^[[:space:]]*[A-Z][a-zA-Z0-9_]+:'; then
			if ! echo "$line" | grep -qE '^[[:space:]]*ConfigureIndexRequest:'; then
				in_configure_index_request=false
			fi
		fi
	fi
	
	# Process nested objects within ConfigureIndexRequest
	if [ "$in_configure_index_request" = true ]; then
		# Look for "serverless:" or "pod:" property (standalone line ending with colon)
		if echo "$line" | grep -qE '^[[:space:]]+(serverless|pod):[[:space:]]*$'; then
			prop_name=$(echo "$line" | sed -E 's/^[[:space:]]+(serverless|pod):[[:space:]]*$/\1/')
			prop_indent=$(echo "$line" | sed 's/^\([[:space:]]*\).*/\1/')
			
			# Look ahead for "type: object" that doesn't have a title
			# Only check next 8 lines to avoid matching other properties
			j=$((i + 1))
			found_type_object=false
			type_object_idx=0
			
			while [ $j -lt $total_lines ] && [ $j -lt $((i + 8)) ]; do
				next_line="${lines[$j]}"
				next_indent=$(echo "$next_line" | sed 's/^\([[:space:]]*\).*/\1/')
				
				# Stop if we've left this property block (less or equal indentation means different property)
				if [ ${#next_indent} -le ${#prop_indent} ]; then
					break
				fi
				
				# Check for type: object (must be at same indent level as other property fields)
				if echo "$next_line" | grep -qE '^[[:space:]]+type:[[:space:]]*object[[:space:]]*$'; then
					found_type_object=true
					type_object_idx=$j
					
					# Check if title already exists in next few lines
					has_title=false
					k=$((j + 1))
					while [ $k -lt $total_lines ] && [ $k -lt $((j + 5)) ]; do
						check_line="${lines[$k]}"
						check_indent=$(echo "$check_line" | sed 's/^\([[:space:]]*\).*/\1/')
						
						# Stop if we've left this object definition
						if [ ${#check_indent} -le ${#next_indent} ]; then
							break
						fi
						
						if echo "$check_line" | grep -qE '^[[:space:]]+title:'; then
							has_title=true
							break
						fi
						if echo "$check_line" | grep -qE '^[[:space:]]+(properties|required|additionalProperties):'; then
							break
						fi
						((k++))
					done
					
					# Add title if needed
					if [ "$has_title" = false ]; then
						# Determine title based on property name
						if [ "$prop_name" = "serverless" ]; then
							title="ConfigureIndexRequestServerlessConfig"
						elif [ "$prop_name" = "pod" ]; then
							title="ConfigureIndexRequestPodBasedConfig"
						else
							break
						fi
						
						# Get indentation from type: object line
						indent=$(echo "${lines[$type_object_idx]}" | sed 's/\(^[[:space:]]*\).*/\1/')
						
						# Insert title after type: object
						insert_idx=$((type_object_idx + 1))
						lines=("${lines[@]:0:$insert_idx}" "${indent}title: ${title}" "${lines[@]:$insert_idx}")
						((total_lines++))
						
						changed=true
						echo "    Added title '${title}' to nested '${prop_name}' object at line $((insert_idx + 1))"
						
						# Skip ahead
						((i = j))
						break
					fi
					break
				fi
				
				((j++))
			done
		fi
	fi
	
	((i++))
done

# Write back if changed
if [ "$changed" = true ]; then
	printf '%s\n' "${lines[@]}" > "$file"
	echo "  File updated successfully"
else
	echo "  No changes needed (titles already exist)"
fi
