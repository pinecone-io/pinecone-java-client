#!/bin/bash

set -eux -o pipefail

# Script to make titles in oneOf blocks unique in YAML/OpenAPI specification files
# Pure shell script with no external dependencies

if [ $# -lt 1 ]; then
	echo "Usage: $0 <yaml_file_or_directory> [--pattern PATTERN]"
	echo ""
	echo "Options:"
	echo "  --pattern PATTERN    Glob pattern for files when processing a directory (default: *.oas.yaml)"
	exit 1
fi

TARGET_PATH="$1"
PATTERN="*.oas.yaml"

# Parse arguments
shift
while [[ $# -gt 0 ]]; do
	case $1 in
		--pattern)
			PATTERN="$2"
			shift 2
			;;
		*)
			echo "Unknown option: $1"
			exit 1
			;;
	esac
done

# Function to get indentation level (number of spaces before content)
get_indent() {
	local line="$1"
	echo "$line" | sed 's/^\([[:space:]]*\).*/\1/' | wc -c | tr -d ' '
}

# Function to make titles unique in a oneOf block
process_oneof_block() {
	local file="$1"
	local oneof_start_line="$2"
	local tmp_file=$(mktemp)
	local changed=false
	
	# Read the file and process the oneOf block
	local in_oneof=false
	local oneof_indent=0
	local current_indent=0
}

# Improved function that handles the file more carefully
process_yaml_file() {
	local file="$1"
	local tmp_file=$(mktemp)
	local changed=false
	
	echo "Processing: $file"
	
	# Find all lines with oneOf:
	local oneof_positions=$(awk '/oneOf:/ {print NR}' "$file")
	
	if [ -z "$oneof_positions" ]; then
		echo "  No oneOf blocks found"
		rm -f "$tmp_file"
		return 0  # No oneOf blocks is not an error
	fi
	
	# Read entire file into array for processing
	local lines=()
	while IFS= read -r line || [ -n "$line" ]; do
		lines+=("$line")
	done < "$file"
	local total_lines=${#lines[@]}
	
	# Process each oneOf block
	while IFS= read -r oneof_line_num; do
		local oneof_line_idx=$((oneof_line_num - 1))
		local oneof_line="${lines[$oneof_line_idx]}"
		
		# Get indentation of oneOf line
		local oneof_indent=$(echo "$oneof_line" | sed 's/^\([[:space:]]*\).*/\1/' | wc -c)
		oneof_indent=$((oneof_indent - 1))
		
		# Find all titles in this oneOf block
		local block_titles=()
		local block_title_indices=()
		
		local i=$((oneof_line_idx + 1))
		while [ $i -lt $total_lines ]; do
			local current_line="${lines[$i]}"
			local current_indent=$(echo "$current_line" | sed 's/^\([[:space:]]*\).*/\1/' | wc -c)
			current_indent=$((current_indent - 1))
			
			# Stop if we've left the oneOf block (same or less indentation as oneOf)
			if [ $current_indent -le $oneof_indent ]; then
				# Check if this is still part of the oneOf (array item continuation)
				if ! echo "$current_line" | grep -qE '^[[:space:]]+-'; then
					break
				fi
			fi
			
			# Look for title lines
			if echo "$current_line" | grep -qE '^[[:space:]]+- title:'; then
				# Extract title value (handle quotes)
				local title=$(echo "$current_line" | sed -E 's/^[[:space:]]+- title:[[:space:]]*["'\'']?([^"'\'']*)["'\'']?[[:space:]]*$/\1/' | sed -E 's/^[[:space:]]+- title:[[:space:]]*([^[:space:]].*)$/\1/')
				block_titles+=("$title")
				block_title_indices+=($i)
			fi
			
			((i++))
		done
		
		# Extract context for this oneOf block (schema name or property name)
		# We add context to ALL titles to ensure global uniqueness
		local context=""
		
		# Look backward to find schema name or property name
		# Check if we're in a schema definition or within a property
		# Skip example blocks and other content
		for ((j=oneof_line_idx-1; j>=0; j--)); do
			local prev_line="${lines[$j]}"
			
			# Skip example blocks and description blocks
			if echo "$prev_line" | grep -qE '^[[:space:]]*(example|description|type|required|additionalProperties|properties):'; then
				continue
			fi
			
			# Look for schema name (e.g., "IndexSpec:")
			# Schema names are typically PascalCase at the components/schemas level
			if echo "$prev_line" | grep -qE '^[[:space:]]+[A-Z][A-Za-z0-9_]*:[[:space:]]*$'; then
				context=$(echo "$prev_line" | sed -E 's/^[[:space:]]+([A-Z][A-Za-z0-9_]*):[[:space:]]*$/\1/')
				break
			fi
			
			# Look for property name (e.g., "    spec:")
			# Property names are typically camelCase or lowercase
			# Must be at same or greater indentation than oneOf
			if echo "$prev_line" | grep -qE '^[[:space:]]+[a-z_][a-z0-9_]*:[[:space:]]*$'; then
				local prev_indent=$(echo "$prev_line" | sed 's/^\([[:space:]]*\).*/\1/' | wc -c)
				prev_indent=$((prev_indent - 1))
				# Only consider properties at same level or above the oneOf
				if [ $prev_indent -le $oneof_indent ]; then
					local prop_name=$(echo "$prev_line" | sed -E 's/^[[:space:]]+([a-z_][a-z0-9_]*):[[:space:]]*$/\1/')
					# Look further back for schema name (within last 100 lines to find parent schema)
					local max_search=$((j > 100 ? j - 100 : 0))
					for ((k=j-1; k>=max_search; k--)); do
						local schema_line="${lines[$k]}"
						# Skip example, description, etc.
						if echo "$schema_line" | grep -qE '^[[:space:]]*(example|description|type|required|additionalProperties|properties):'; then
							continue
						fi
						# Schema names start with uppercase and are at components/schemas level
						# Check if this is a schema definition (starts with uppercase, ends with colon, no content)
						if echo "$schema_line" | grep -qE '^[[:space:]]+[A-Z][A-Za-z0-9_]*:[[:space:]]*$'; then
							local schema_indent=$(echo "$schema_line" | sed 's/^\([[:space:]]*\).*/\1/' | wc -c)
							schema_indent=$((schema_indent - 1))
							# Schema should be at same or less indentation than the property
							if [ $schema_indent -le $prev_indent ]; then
								local schema_name=$(echo "$schema_line" | sed -E 's/^[[:space:]]+([A-Z][A-Za-z0-9_]*):[[:space:]]*$/\1/')
								# Use just the schema name (more semantic than schema + property)
								context="$schema_name"
								break
							fi
						fi
					done
					# If no schema found, use property name as fallback
					if [ -z "$context" ]; then
						context="$prop_name"
					fi
					break
				fi
			fi
		done
		
		# Modify all titles to include context for global uniqueness
		# Since parent names are unique, prepending them makes all titles unique
		for idx in "${!block_title_indices[@]}"; do
			local i=${block_title_indices[$idx]}
			local title="${block_titles[$idx]}"
			
			# Use Java-friendly format (no spaces, PascalCase)
			local new_title=""
			
			if [ -n "$context" ]; then
				# Convert context to PascalCase
				# If context already contains PascalCase words (schema names), preserve them
				# Split on spaces, handle each word
				local pascal_context=""
				for word in $context; do
					# Check if word is already PascalCase (starts with uppercase and has mixed case)
					if echo "$word" | grep -qE '^[A-Z][a-z]'; then
						# Already PascalCase, use as-is
						pascal_context="${pascal_context}${word}"
					else
						# Convert to PascalCase: capitalize first letter, lowercase rest
						local first=$(echo "$word" | cut -c1 | tr '[:lower:]' '[:upper:]')
						local rest=$(echo "$word" | cut -c2- | tr '[:upper:]' '[:lower:]')
						pascal_context="${pascal_context}${first}${rest}"
					fi
				done
				
				# Check if title already starts with the context (to prevent double-processing)
				# Escape the context for regex matching and check if title already contains it
				local escaped_context=$(echo "$pascal_context" | sed 's/[[\.*^$()+?{|]/\\&/g')
				if echo "$title" | grep -qE "^${escaped_context}"; then
					# Title already has context, don't add it again
					new_title="$title"
				else
					# Prepend parent context without space (Java-friendly)
					new_title="${pascal_context}${title}"
				fi
			else
				# Fallback: keep original title if no context found
				new_title="$title"
			fi
			
			# Only update if the title actually changed
			if [ "$new_title" != "$title" ]; then
				local original_line="${lines[$i]}"
				# Extract indentation from original line (preserve spaces/tabs)
				local indent=$(echo "$original_line" | sed 's/\(^[[:space:]]*\).*/\1/')
				
				# Preserve quoting style
				if echo "$original_line" | grep -qE '^[[:space:]]+- title:[[:space:]]*"'; then
					lines[$i]="${indent}- title: \"${new_title}\""
				elif echo "$original_line" | grep -qE "^[[:space:]]+- title:[[:space:]]*'"; then
					lines[$i]="${indent}- title: '${new_title}'"
				else
					lines[$i]="${indent}- title: ${new_title}"
				fi
				
				changed=true
				echo "    Changed title '$title' to '$new_title' at line $((i+1))"
			fi
		done
	done <<< "$oneof_positions"
	
	# Write back if changed
	if [ "$changed" = true ]; then
		printf '%s\n' "${lines[@]}" > "$tmp_file"
		mv "$tmp_file" "$file"
		echo "  File updated successfully"
		return 0
	else
		rm -f "$tmp_file"
		echo "  No duplicate titles found"
		return 0  # No changes is not an error
	fi
}

process_file() {
	local file="$1"
	
	if [ ! -f "$file" ]; then
		echo "Error: File does not exist: $file"
		return 1
	fi
	
	process_yaml_file "$file"
}

process_directory() {
	local dir="$1"
	local pattern="$2"
	
	if [ ! -d "$dir" ]; then
		echo "Error: Directory does not exist: $dir"
		exit 1
	fi
	
	local count=0
	local changed_count=0
	
	# Find and process all matching files
	while IFS= read -r -d '' file; do
		count=$((count + 1))
		if process_file "$file"; then
			changed_count=$((changed_count + 1))
		fi
	done < <(find "$dir" -maxdepth 1 -type f -name "$pattern" -print0 2>/dev/null)
	
	if [ $count -eq 0 ]; then
		echo "No files matching pattern '$pattern' found in $dir"
		return
	fi
	
	echo ""
	echo "Processed $count file(s), made titles unique in $changed_count file(s)"
}

# Main execution
if [ -f "$TARGET_PATH" ]; then
	process_file "$TARGET_PATH"
elif [ -d "$TARGET_PATH" ]; then
	process_directory "$TARGET_PATH" "$PATTERN"
else
	echo "Error: Path is neither a file nor a directory: $TARGET_PATH"
	exit 1
fi

